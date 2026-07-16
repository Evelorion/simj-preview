/* SIMJ full globe app — static file on VPS, cached by browser */
(()=>{
'use strict';

// ---- progressive asset loader (files already on VPS) ----
function simjFetchJson(url){return fetch(url,{cache:'force-cache'}).then(r=>{if(!r.ok)throw new Error(url+' '+r.status);return r.json();});}
function simjSetStatus(msg){const el=document.getElementById('status-text');if(el)el.textContent=msg||'';}
function simjHideStatus(){const el=document.getElementById('status');if(el)el.classList.add('hidden');}

const EARTH_TEXTURE="/assets/earth.jpg";
const EARTH_BORDER_TEXTURE="/assets/earth-border.jpg";
const OUTLINE_PLAIN_TEXTURE="/assets/outline-plain.png";
const OUTLINE_BORDER_TEXTURE="/assets/outline-border.png";
let GEOJSON=null; /* loaded from /data/countries.geojson */
const OUTLINE_TEXTURE=OUTLINE_BORDER_TEXTURE;
const TRANSPARENT_TEXTURE="/assets/transparent.png";
/* removed night mode texture */
const UNUSED_NIGHT_TEXTURE="/assets/unused-night.jpg";
/* removed night lights texture */
const UNUSED_LIGHTS_TEXTURE="/assets/unused-lights.png";
const CLOUD_TEXTURE="/assets/cloud.png";
const $=id=>document.getElementById(id);
const state={globe:null,countries:[],boundaries:[],borderPaths:[],borderPathsLite:[],borderPathsDetail:[],borderLod:'lite',countryGrid:null,selected:null,hovered:null,borders:true,rotating:true,atmosphere:true,outlineMode:false,tileMode:'satellite',activeTileMode:null,currentTexture:null,smallCountryPoints:[],filtered:[],searchIndex:-1,pointerDown:null,raf:0,lastPointer:null,accurateBoundaryCache:new Map(),baseBoundaryOverrides:new Map(),selectedBoundaryPaths:[],hoverBoundaryPaths:[],boundaryRequestToken:0,hoverBoundaryToken:0,hoverBoundaryTimer:0,interacting:false,bordersHiddenForDrag:false,quality:{dpr:1.5,aa:true,tileMax:11,curve:0.42,lodTimer:0,lastAlt:-1,settleTimer:0}};
const TILE_MODE_ORDER=['satellite','street','offline'];
// ---- Adaptive sharpness: NEVER change mesh/LOD while dragging (stutter killer) ----
function simjIsLowEnd(){
  try{
    const mem=navigator.deviceMemory||8;
    const cores=navigator.hardwareConcurrency||4;
    const dpr=window.devicePixelRatio||1;
    const mobile=/Mobi|Android|iPhone|iPad/i.test(navigator.userAgent||'');
    return mem<=4 || cores<=4 || (mobile && dpr>=3);
  }catch(e){return false}
}
function simjComputeDpr(){
  const raw=window.devicePixelRatio||1;
  const low=simjIsLowEnd();
  // Hard cap — high DPR is a major freeze source with paths + tiles
  if(low) return Math.min(raw, 1.1);
  if(raw>=2) return Math.min(raw, 1.25);
  return Math.min(Math.max(raw, 1.0), 1.25);
}
function simjTileMaxForAltitude(alt, mode){
  const a=Number(alt)||1.8;
  // Interact / micro-fly: hard cap tiles — high zoom is the main freeze source
  const interacting=!!state.interacting;
  const microHold=state.quality.microHoldUntil && Date.now()<state.quality.microHoldUntil;
  let max=11;
  if(a>1.7) max=interacting?9:11;
  else if(a>1.3) max=interacting?10:12;
  else if(a>0.9) max=interacting?10:12;
  else if(a>0.55) max=interacting?11:12;
  else if(a>0.35) max=interacting?11:12;
  else max=interacting?11:12;
  if(microHold) max=Math.min(max, 11);
  if(mode==='street') max=Math.min(max+1, 13);
  if(simjIsLowEnd()) max=Math.min(max, 11);
  return max;
}
function simjCurveForAltitude(alt){
  // FIXED curve always — changing curvature remeshes the sphere (major stutter on zoom)
  return 0.42;
}
function simjApplyAdaptiveLod(force){
  if(!state.globe||state.outlineMode||state.tileMode==='offline') return;
  // Critical: skip all LOD work while user is rotating/zooming
  if(state.interacting && !force) return;
  const pov=state.globe.pointOfView?.()||{};
  let alt=pov.altitude??1.8;
  // Never go so close that tiles + paths freeze the main thread
  if(alt<0.32){
    try{ state.globe.pointOfView({lat:pov.lat,lng:pov.lng,altitude:0.36},0); alt=0.36; }catch(e){}
  }
  if(!force && state.quality.lastAlt>=0 && Math.abs(alt-state.quality.lastAlt)<0.12) return;
  state.quality.lastAlt=alt;
  const maxLv=simjTileMaxForAltitude(alt, state.tileMode);
  if(force || state.quality.tileMax!==maxLv){
    state.quality.tileMax=maxLv;
    try{ state.globe.globeTileEngineMaxLevel(maxLv); }catch(e){}
  }
  // NEVER touch curvature after init
  // Detail borders only when user manually zooms very close AND not in micro-hold
  // (auto fly-to used to force detail rebuild → freeze on small countries)
  const microHold=state.quality.microHoldUntil && Date.now()<state.quality.microHoldUntil;
  if(!state.interacting && !microHold){
    // Prefer lite almost always; detail only extremely close + idle
    const want=(alt>0.45)?'lite':'lite'; // keep lite always for stability (detail optional later)
    if(state.borderLod!==want){
      state.borderLod=want;
      state.borderPaths=state.borderPathsLite||state.borderPathsDetail||state.borderPaths;
      try{ refreshBorderPaths(); }catch(e){}
    }
  }else if(microHold && state.borderLod!=='lite'){
    state.borderLod='lite';
    state.borderPaths=state.borderPathsLite||state.borderPaths;
  }
}
function simjScheduleLod(){
  if(state.interacting) return;
  if(state.quality.lodTimer) clearTimeout(state.quality.lodTimer);
  state.quality.lodTimer=setTimeout(()=>simjApplyAdaptiveLod(false), 220);
}
function simjSetRendererDpr(ratio){
  try{
    const r=state.globe?.renderer?.();
    if(r) r.setPixelRatio(ratio);
  }catch(e){}
}
function simjBeginInteract(){
  if(state.interacting) return;
  state.interacting=true;
  if(state.quality.lodTimer){ clearTimeout(state.quality.lodTimer); state.quality.lodTimer=0; }
  if(state.quality.settleTimer){ clearTimeout(state.quality.settleTimer); state.quality.settleTimer=0; }
  // Keep borders visible (hiding them made lines "flash/mess"). Only drop DPR.
  simjSetRendererDpr(1.05);
  hideTip();
  document.body.style.cursor='grabbing';
}
function simjEndInteract(){
  state.interacting=false;
  document.body.style.cursor='default';
  simjSetRendererDpr(Math.min(state.quality.dpr||1.35, 1.3));
  if(state.quality.settleTimer) clearTimeout(state.quality.settleTimer);
  state.quality.settleTimer=setTimeout(()=>{
    state.quality.settleTimer=0;
    try{ simjApplyAdaptiveLod(true); }catch(e){}
  }, 200);
}
function arcgisHiDpiTileUrl(x,y,z){return 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/'+z+'/'+y+'/'+x}
// maxLevel here is ceiling; adaptive LOD lowers it when far for speed
const TILE_SOURCES={satellite:{label:'卫星',maxLevel:18,url:arcgisHiDpiTileUrl,attribution:'Tiles © Esri — Source: Esri, Maxar, Earthstar Geographics, and the GIS User Community'},street:{label:'地图',maxLevel:19,url:(x,y,l)=>`https://tile.openstreetmap.org/${l}/${x}/${y}.png`,attribution:'© OpenStreetMap contributors'},offline:{label:'离线',maxLevel:0,url:null,attribution:'离线内置贴图'}};
const SMALL_COUNTRY_FORCE_ISOS=new Set(['AD','MC','SM','VA','LI','LU','MT','SG','BH','QA','BN','HK','MO','CY','KM','MU','CV','ST','SC','DM','GD','KN','LC','VC','AG','BB','MV','NR','TV','WS','TO','KI','MH','FM','PW','NR','SR','TL']);
const COMPACT_MICROSTATE_ISOS=new Set(['AD','MC','SM','VA','LI','LU','MT','SG','BH','HK','MO']);

const regionNames=(()=>{try{return new Intl.DisplayNames(['zh-CN'],{type:'region'})}catch(e){return null}})();
const ZH_NAME_OVERRIDES={TW:'中国台湾省',HK:'中国香港特别行政区',MO:'中国澳门特别行政区'};
const EN_NAME_OVERRIDES={TW:'Taiwan, China',HK:'Hong Kong',MO:'Macao'};
const ISO_NAME_OVERRIDES={"France":["FR","FRA"],"Norway":["NO","NOR"],"Somaliland":["SO","SOM"],"Northern Cyprus":["CY","CYP"],"Kosovo":["XK","XKX"],"Taiwan":["TW","TWN"],"Taiwan, China":["TW","TWN"],"Chinese Taipei":["TW","TWN"]};
const PHONE_LENGTH_HINTS={"AC":"5位","AD":"6位 / 9位","AE":"9位","AF":"9位","AG":"10位","AI":"10位","AL":"9位","AM":"8位","AO":"9位","AR":"10–11位","AS":"10位","AT":"7–13位","AU":"9位","AW":"7位","AX":"6–10位","AZ":"9位","BA":"8–9位","BB":"10位","BD":"10位","BE":"9位","BF":"8位","BG":"8–9位","BH":"8位","BI":"8位","BJ":"10位","BL":"9位","BM":"10位","BN":"7位","BO":"8位","BQ":"7位","BR":"10–11位","BS":"10位","BT":"8位","BW":"8位","BY":"9位","BZ":"7位","CA":"10位","CC":"9位","CD":"7位 / 9位","CF":"8位","CG":"9位","CH":"9位","CI":"10位","CK":"5位","CL":"9位","CM":"9位","CN":"11位","CO":"10位","CR":"8位","CU":"8位","CV":"7位","CW":"7–8位","CX":"9位","CY":"8位","CZ":"9位","DE":"10–11位","DJ":"8位","DK":"8位","DM":"10位","DO":"10位","DZ":"9位","EC":"9位","EE":"7–8位","EG":"10位","EH":"9位","ER":"7位","ES":"9位","ET":"9位","FI":"6–10位","FJ":"7位","FK":"5位","FM":"7位","FO":"6位","FR":"9位","GA":"7–8位","GB":"10位","GD":"10位","GE":"9位","GF":"9位","GG":"10位","GH":"9位","GI":"8位","GL":"6位","GM":"7位","GN":"9位","GP":"9位","GQ":"9位","GR":"10位","GT":"8位","GU":"10位","GW":"9位","GY":"7位","HK":"8位","HN":"8位","HR":"8–9位","HT":"8位","HU":"9位","ID":"9–12位","IE":"9位","IL":"9位","IM":"10位","IN":"10位","IO":"7位","IQ":"10位","IR":"10位","IS":"7位 / 9位","IT":"9–10位","JE":"10位","JM":"10位","JO":"9位","JP":"10位","KE":"9位","KG":"9位","KH":"8–9位","KI":"8位","KM":"7位","KN":"10位","KP":"10位","KR":"9–10位","KW":"8位","KY":"10位","KZ":"10位","LA":"9–10位","LB":"7–8位","LC":"10位","LI":"7位 / 9位","LK":"9位","LR":"7位 / 9位","LS":"8位","LT":"8位","LU":"9位","LV":"8位","LY":"9位","MA":"9位","MC":"8–9位","MD":"8位","ME":"8位","MF":"9位","MG":"9位","MH":"7位","MK":"8位","ML":"8位","MM":"7–10位","MN":"8位","MO":"8位","MP":"10位","MQ":"9位","MR":"8位","MS":"10位","MT":"8位","MU":"8位","MV":"7位","MW":"9位","MX":"10位","MY":"9–10位","MZ":"9位","NA":"9位","NC":"6位","NE":"8位","NF":"6位","NG":"10位","NI":"8位","NL":"9位 / 11位","NO":"8位","NP":"10位","NR":"7位","NU":"4位 / 7位","NZ":"8–10位","OM":"8位","PA":"7–8位","PE":"9位","PF":"8位","PG":"8位","PH":"10位","PK":"10位","PL":"9位","PM":"6位 / 9位","PR":"10位","PS":"9位","PT":"9位","PW":"7位","PY":"9位","QA":"8位","RE":"9位","RO":"9位","RS":"8–10位","RU":"10位","RW":"9位","SA":"9位","SB":"5位 / 7位","SC":"7位","SD":"9位","SE":"9位","SG":"8位","SH":"5位","SI":"8位","SJ":"8位","SK":"9位","SL":"8位","SM":"8位","SN":"9位","SO":"7–9位","SR":"7位","SS":"9位","ST":"7位","SV":"8位","SX":"10位","SY":"9位","SZ":"8位","TA":"暂无统一数据","TC":"10位","TD":"8位","TG":"8位","TH":"9位","TJ":"9位","TK":"4–7位","TL":"8位","TM":"8位","TN":"8位","TO":"7位","TR":"10位","TT":"10位","TV":"6–7位","TW":"9位","TZ":"9位","UA":"9位","UG":"9位","US":"10位","UY":"8位","UZ":"9位","VA":"9–10位","VC":"10位","VE":"10位","VG":"10位","VI":"10位","VN":"9位","VU":"7位","WF":"6位","WS":"7位 / 10位","XK":"8位","YE":"9位","YT":"9位","ZA":"5–9位","ZM":"9位","ZW":"9位"};
let MICRO_FEATURES=[]; /* loaded from /data/micro-features.json */
function injectSpecialFeatures(features){const list=[...features];const isoSet=new Set(list.map(f=>String(f?.properties?.ISO_A2||'').toUpperCase()));for(const f of MICRO_FEATURES){const iso=String(f?.properties?.ISO_A2||'').toUpperCase();if(!isoSet.has(iso)){list.push(f);isoSet.add(iso)}}return list}

let OFFLINE_COUNTRY_META=null; /* loaded from /data/offline-country-meta.json */
let SPECIAL_COUNTRY_META=null; /* loaded from /data/special-country-meta.json */

function esc(v){return String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]))}
function normLng(v){let x=v;while(x>180)x-=360;while(x<-180)x+=360;return x}
function unwrapRing(ring){if(!ring?.length)return[];const out=[];let prev=ring[0][0];for(let i=0;i<ring.length;i++){let x=ring[i][0],y=ring[i][1];if(i){while(x-prev>180)x-=360;while(x-prev<-180)x+=360}out.push([x,y]);prev=x}return out}
function prepRing(ring){const pts=unwrapRing(ring);let minX=Infinity,maxX=-Infinity,minY=Infinity,maxY=-Infinity,sumX=0;for(const [x,y] of pts){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);sumX+=x}return{pts,minX,maxX,minY,maxY,centerX:pts.length?sumX/pts.length:0}}
function ringArea(r){const p=r.pts;let a=0;for(let i=0,j=p.length-1;i<p.length;j=i++)a+=p[j][0]*p[i][1]-p[i][0]*p[j][1];return Math.abs(a/2)}
function ringContains(r,lng,lat){if(lat<r.minY||lat>r.maxY||r.pts.length<3)return false;let x=lng;while(x-r.centerX>180)x-=360;while(x-r.centerX<-180)x+=360;if(x<r.minX||x>r.maxX)return false;let inside=false;const pts=r.pts;for(let i=0,j=pts.length-1;i<pts.length;j=i++){const xi=pts[i][0],yi=pts[i][1],xj=pts[j][0],yj=pts[j][1];if(((yi>lat)!==(yj>lat))&&(x<(xj-xi)*(lat-yi)/((yj-yi)||Number.EPSILON)+xi))inside=!inside}return inside}
function featureContains(c,lat,lng){if(lat<c.__minLat||lat>c.__maxLat)return false;for(const poly of c.__polys){if(!ringContains(poly[0],lng,lat))continue;let hole=false;for(let i=1;i<poly.length;i++)if(ringContains(poly[i],lng,lat)){hole=true;break}if(!hole)return true}return false}
function countryKey(c){if(!c)return'';const p=c.properties||{};const iso3=String(c.__iso3||p.ISO_A3||p['ISO3166-1-Alpha-3']||'').toUpperCase();const iso2=String(c.__iso2||p.ISO_A2||p['ISO3166-1-Alpha-2']||'').toUpperCase();return(iso3&&iso3!=='-99'?iso3:'')||(iso2&&iso2!=='-99'?iso2:'')||c.__nameEn||c.__nameZh||p.ADMIN||p.NAME||p.name||''}
function centerDistance(coords,c){let dlng=Math.abs(coords.lng-c.__center.lng);if(dlng>180)dlng=360-dlng;const dx=dlng*Math.cos(coords.lat*Math.PI/180),dy=coords.lat-c.__center.lat;return Math.hypot(dx,dy)}
function buildCountryGrid(countries){
  const cell=8; // degrees
  const map=new Map();
  for(const c of countries){
    if(!Number.isFinite(c.__minLat)||!Number.isFinite(c.__maxLat)) continue;
    const lat0=Math.max(-90,Math.floor(c.__minLat/cell));
    const lat1=Math.min(90,Math.floor(c.__maxLat/cell));
    // lng may be unwrapped >180; clamp roughly for index
    let minLng=c.__center?.lng??0, maxLng=minLng;
    for(const poly of (c.__polys||[])){
      const r=poly[0]; if(!r) continue;
      minLng=Math.min(minLng,r.minX); maxLng=Math.max(maxLng,r.maxX);
    }
    const lng0=Math.floor(minLng/cell), lng1=Math.floor(maxLng/cell);
    for(let iy=lat0; iy<=lat1; iy++){
      for(let ix=lng0; ix<=lng1; ix++){
        const k=ix+','+iy;
        let arr=map.get(k); if(!arr){arr=[]; map.set(k,arr);}
        arr.push(c);
      }
    }
  }
  return {cell,map};
}
function countryAt(coords){
  if(!coords||!Number.isFinite(coords.lat)||!Number.isFinite(coords.lng))return null;
  // Microstates first (tiny set)
  let micro=null,microD=Infinity;
  for(const c of state.countries){
    if(!COMPACT_MICROSTATE_ISOS.has(c.__iso2))continue;
    const d=centerDistance(coords,c);
    const tol=c.__iso2==='AD'?.27:c.__iso2==='MC'||c.__iso2==='VA'?.12:.18;
    if(d<tol&&d<microD){micro=c;microD=d}
  }
  if(micro)return micro;
  // Spatial grid candidates (avoid scanning every country ring on each click)
  let candidates=state.countries;
  const grid=state.countryGrid;
  if(grid){
    const ix=Math.floor(coords.lng/grid.cell), iy=Math.floor(coords.lat/grid.cell);
    const seen=new Set(); const list=[];
    for(let dy=-1;dy<=1;dy++)for(let dx=-1;dx<=1;dx++){
      const arr=grid.map.get((ix+dx)+','+(iy+dy));
      if(!arr)continue;
      for(const c of arr){ if(!seen.has(c)){ seen.add(c); list.push(c);} }
    }
    if(list.length) candidates=list;
  }
  const hits=[];
  for(const c of candidates) if(featureContains(c,coords.lat,coords.lng)) hits.push(c);
  hits.sort((a,b)=>a.__area-b.__area);
  if(hits[0])return hits[0];
  let best=null,bestD=Infinity;
  for(const c of state.countries){
    if(!c.__isSmall)continue;
    const d=centerDistance(coords,c);
    const tol=Math.max(.08,Math.min(.24,Math.max(c.__latSpan||0,c.__lngSpan||0)*.7));
    if(d<tol&&d<bestD){best=c;bestD=d}
  }
  return best;
}
function prepareCountries(features){return injectSpecialFeatures(features).map((f,index)=>{const p=f.properties||{},g=f.geometry||{};const rawPolys=g.type==='Polygon'?[g.coordinates]:g.type==='MultiPolygon'?(g.coordinates||[]):[];const polys=rawPolys.map(poly=>poly.map(prepRing));let minLat=Infinity,maxLat=-Infinity,minLng=Infinity,maxLng=-Infinity,area=0;for(const poly of polys)for(const ring of poly){minLat=Math.min(minLat,ring.minY);maxLat=Math.max(maxLat,ring.maxY);minLng=Math.min(minLng,ring.minX);maxLng=Math.max(maxLng,ring.maxX)}for(const poly of polys)if(poly[0])area+=ringArea(poly[0]);let iso2=(p.ISO_A2||p.iso2||'').toUpperCase(),iso3=(p.ISO_A3||p.iso3||'').toUpperCase();if(iso2==='-99') iso2='';if(iso3==='-99') iso3='';const isoFix=ISO_NAME_OVERRIDES[p.ADMIN||p.NAME];if(isoFix){iso2=isoFix[0];iso3=isoFix[1]}
// Taiwan often has invalid ISO_A2=-99 in Natural Earth — force TW so flag/meta work
const adminName=String(p.ADMIN||p.NAME||p.name||'');
if((!iso2||iso2==='-99')&&/taiwan|台湾|台灣/i.test(adminName)){iso2='TW';iso3=iso3||'TWN'}
if(iso2==='TW'||/taiwan|台湾|台灣/i.test(adminName)){iso2='TW';iso3='TWN'}
let zh=ZH_NAME_OVERRIDES[iso2]||p.ADMIN||p.NAME||'Unknown';if(!ZH_NAME_OVERRIDES[iso2]&&regionNames&&iso2&&iso2!=='-99'){try{zh=regionNames.of(iso2)||zh}catch(e){}}const en=EN_NAME_OVERRIDES[iso2]||p.ADMIN||p.NAME||'Unknown';let best=null,bestArea=-1;for(const poly of polys)if(poly[0]){const a=ringArea(poly[0]);if(a>bestArea){bestArea=a;best=poly[0]}}let center={lat:0,lng:0};if(best?.pts?.length){let sx=0,sy=0,sz=0;for(const [lng,lat] of best.pts){const ph=lat*Math.PI/180,la=lng*Math.PI/180;sx+=Math.cos(ph)*Math.cos(la);sy+=Math.cos(ph)*Math.sin(la);sz+=Math.sin(ph)}center.lng=normLng(Math.atan2(sy,sx)*180/Math.PI);center.lat=Math.atan2(sz,Math.sqrt(sx*sx+sy*sy))*180/Math.PI;if(!featureContains({__polys:polys,__minLat:minLat,__maxLat:maxLat},center.lat,center.lng)){const q=best.pts[Math.floor(best.pts.length/2)];center={lng:normLng(q[0]),lat:q[1]}}}let lngSpan=(Number.isFinite(minLng)&&Number.isFinite(maxLng))?(maxLng-minLng):0;while(lngSpan>360)lngSpan-=360;if(lngSpan<0)lngSpan=Math.abs(lngSpan);if(lngSpan>180)lngSpan=360-lngSpan;const latSpan=(Number.isFinite(minLat)&&Number.isFinite(maxLat))?(maxLat-minLat):0;f.__index=index;f.__polys=polys;f.__minLat=minLat;f.__maxLat=maxLat;f.__area=area||Infinity;f.__latSpan=latSpan;f.__lngSpan=lngSpan;f.__bboxArea=latSpan*lngSpan;f.__nameZh=zh;f.__nameEn=en;f.__iso2=iso2;f.__iso3=iso3;f.__center=center;f.__key=countryKey(f);f.__search=`${zh} ${en} ${p.ADMIN||''} ${p.NAME||''} ${iso2} ${iso3}`.toLowerCase();return f}).sort((a,b)=>a.__nameZh.localeCompare(b.__nameZh,'zh-CN'))}
function scaleGeometryAround(geometry,center,factor){const scalePoint=p=>{let lng=p[0],lat=p[1],dlng=lng-center.lng;while(dlng>180)dlng-=360;while(dlng<-180)dlng+=360;return[normLng(center.lng+dlng*factor),Math.max(-89.9,Math.min(89.9,center.lat+(lat-center.lat)*factor))]};const walk=obj=>Array.isArray(obj)&&obj.length&&typeof obj[0]==='number'?scalePoint(obj):obj.map(walk);return{type:geometry.type,coordinates:walk(geometry.coordinates)}}
function makeMicroDisplayGeometry(c){return c.geometry}
function buildSmallCountryPoints(countries){return countries.filter(c=>{const iso=c.__iso2||'';const forced=SMALL_COUNTRY_FORCE_ISOS.has(iso);const compact=(c.__bboxArea>0&&c.__bboxArea<=3.6)||(c.__latSpan<=1.35&&c.__lngSpan<=3.4)||(c.__latSpan<=2.2&&c.__lngSpan<=1.5);const isSmall=forced||compact;if(isSmall){c.__isSmall=true;c.__displayGeometry=makeMicroDisplayGeometry(c)}return isSmall}).map(c=>({lat:c.__center.lat,lng:c.__center.lng,name:c.__nameZh,type:'micro',iso:c.__iso2||c.__iso3||''}))}
function outlineBackground(on){document.body.style.background=on?'#dfe5ef':'radial-gradient(1200px 800px at 50% 46%, #08326b 0%, #041229 55%, #020814 100%)'}
function chosenTexture(){
  if(state.outlineMode)return state.borders?OUTLINE_BORDER_TEXTURE:OUTLINE_PLAIN_TEXTURE;
  // Prefer full earth textures when present; fall back to outline assets if heavy files not on VPS yet
  return state.borders?(EARTH_BORDER_TEXTURE||OUTLINE_BORDER_TEXTURE):(EARTH_TEXTURE||OUTLINE_PLAIN_TEXTURE)
}
function updateStableTexture(force=false){if(!state.globe)return;const next=chosenTexture();if(!force&&state.currentTexture===next)return;state.currentTexture=next;state.globe.globeImageUrl(next);requestAnimationFrame(()=>requestAnimationFrame(applyTextureTuning))}
// Douglas–Peucker: keeps coastline shape correct (unlike fixed stride which "broke" lines)
function simplifyRingRDP(ring,eps){
  if(!ring||ring.length<5||!(eps>0)) return ring||[];
  const eps2=eps*eps;
  function dist2(a,b,p){
    const x=a[0],y=a[1],x2=b[0],y2=b[1],px=p[0],py=p[1];
    const dx=x2-x,dy=y2-y,len2=dx*dx+dy*dy;
    if(len2<1e-18){const ddx=px-x,ddy=py-y;return ddx*ddx+ddy*ddy}
    let t=((px-x)*dx+(py-y)*dy)/len2; t=t<0?0:t>1?1:t;
    const qx=x+t*dx,qy=y+t*dy,ddx=px-qx,ddy=py-qy; return ddx*ddx+ddy*ddy;
  }
  function rec(pts,a,b,keep){
    let maxD=-1,idx=-1;
    for(let i=a+1;i<b;i++){const d=dist2(pts[a],pts[b],pts[i]); if(d>maxD){maxD=d;idx=i;}}
    if(maxD>eps2&&idx>=0){rec(pts,a,idx,keep); keep[idx]=1; rec(pts,idx,b,keep);}
  }
  let closed=false; let pts=ring;
  if(pts.length>2){
    const a=pts[0],b=pts[pts.length-1];
    if(Math.abs(a[0]-b[0])<1e-9&&Math.abs(a[1]-b[1])<1e-9){closed=true;pts=pts.slice(0,-1);}
  }
  if(pts.length<4) return ring;
  const keep=new Uint8Array(pts.length); keep[0]=1; keep[pts.length-1]=1;
  rec(pts,0,pts.length-1,keep);
  const out=[];
  for(let i=0;i<pts.length;i++) if(keep[i]) out.push(pts[i]);
  if(closed&&out.length) out.push(out[0]);
  return out.length>=4?out:ring;
}
function ringSignedArea(ring){
  let a=0;
  if(!ring||ring.length<3) return 0;
  for(let i=0,j=ring.length-1;i<ring.length;j=i++) a+=ring[j][0]*ring[i][1]-ring[i][0]*ring[j][1];
  return Math.abs(a/2);
}
function downsampleRing(ring,maxPts){
  if(!ring||ring.length<=maxPts) return ring;
  const step=Math.ceil(ring.length/maxPts);
  const out=[];
  for(let i=0;i<ring.length;i+=step) out.push(ring[i]);
  const last=ring[ring.length-1], first=out[0];
  if(last&&first&&(last[0]!==first[0]||last[1]!==first[1])) out.push(last);
  return out;
}
// Blue palette — eSIM + selection are all blue family (user request)
const SIMJ_BLUE={
  border:'rgba(210,230,255,.72)',
  esimCap:'rgba(56,189,248,.52)',
  esimSide:'rgba(14,116,180,.28)',
  esimStroke:'rgba(125,211,252,1)',
  selCap:'rgba(14,165,233,.62)',
  selSide:'rgba(2,132,199,.32)',
  selStroke:'rgba(186,230,253,1)',
  pathSel:'rgba(56,189,248,1)',
};
/** Split ring into continuous segments at antimeridian (never draw slash across globe) */
function splitRingForGlobePath(ring){
  if(!ring||ring.length<2) return [];
  // Normalize each point to [-180,180]; break when successive points would jump >90° in lng
  const norm=ring.map(p=>[normLng(p[0]), Math.max(-89.9, Math.min(89.9, p[1]))]);
  const segs=[];
  let cur=[{lng:norm[0][0], lat:norm[0][1]}];
  for(let i=1;i<norm.length;i++){
    const prev=norm[i-1], pt=norm[i];
    let dl=pt[0]-prev[0];
    // smallest signed delta in [-180,180]
    while(dl>180) dl-=360; while(dl<-180) dl+=360;
    const dlat=Math.abs(pt[1]-prev[1]);
    // large jump = antimeridian or bad simplify chord — start new segment
    if(Math.abs(dl)>90 || dlat>60){
      if(cur.length>=2) segs.push(cur);
      cur=[{lng:pt[0], lat:pt[1]}];
    }else{
      cur.push({lng:pt[0], lat:pt[1]});
    }
  }
  if(cur.length>=2) segs.push(cur);
  return segs;
}
function geometryBorderPaths(geometry,c,key,kind='base',eps=0,opts){
  // Clean globe borders:
  //  - do NOT unwrap longitudes for path drawing (causes Antarctica/Russia slash lines)
  //  - split at antimeridian instead
  //  - drop tiny island rings + polar Antarctica soup for base world borders
  const out=[];
  if(!geometry)return out;
  const iso=String(c?.__iso2||c?.properties?.ISO_A2||'').toUpperCase();
  const name=String(c?.__nameEn||c?.properties?.ADMIN||c?.properties?.NAME||'');
  const isAntarctica=iso==='AQ'||/antarctica/i.test(name);
  const closedRings=!!(opts&&opts.closedRings); // true for fill polygons
  const maxRings=(opts&&opts.maxRings!=null)?opts.maxRings:(kind==='selected-core'?6:3);
  const maxPts=(opts&&opts.maxPts)||(kind==='selected-core'?220:180);
  const minRel=(opts&&opts.minRelArea)!=null?(opts.minRelArea):(kind==='selected-core'?0.015:0.05);
  // Base world map: skip Antarctica (polar rings always look messy on globe.gl paths)
  if(!closedRings && kind==='base' && isAntarctica) return out;
  const polys=geometry.type==='Polygon'?[geometry.coordinates]:geometry.type==='MultiPolygon'?(geometry.coordinates||[]):[];
  let rings=[];
  for(const poly of polys){
    if(!poly?.length)continue;
    let ring=poly[0];
    if(!ring||ring.length<3)continue;
    // Keep original coords; only unwrap for area ranking of multi-island countries
    rings.push(ring);
  }
  if(rings.length>1){
    // rank by unwrapped area so main landmass wins
    rings.sort((a,b)=>ringSignedArea(unwrapRing(b))-ringSignedArea(unwrapRing(a)));
    const main=ringSignedArea(unwrapRing(rings[0]))||1;
    rings=rings.filter((r,i)=>i===0||ringSignedArea(unwrapRing(r))>=main*minRel).slice(0,maxRings);
  }else if(rings.length>maxRings){
    rings=rings.slice(0,maxRings);
  }
  for(let ring of rings){
    // Mild RDP on unwrapped copy, then map back — preserves shape without fixed-stride kinks
    let work=unwrapRing(ring);
    if(eps>0&&work.length>20){
      const simplified=simplifyRingRDP(work,eps);
      if(simplified&&simplified.length>=4) work=simplified;
    }
    // Prefer denser coastal sampling over crude stride downsample (stride = broken lines)
    if(work.length>maxPts){
      // progressive RDP with slightly larger eps instead of stride
      let e=Math.max(eps||0.02, 0.02);
      for(let t=0;t<6 && work.length>maxPts;t++){
        e*=1.35;
        const s=simplifyRingRDP(work,e);
        if(s&&s.length>=4) work=s; else break;
      }
      if(work.length>maxPts) work=downsampleRing(work,maxPts);
    }
    if(closedRings){
      // closed polygon rings for fill: re-normalize lng into continuous unwrap for the ring only
      const pts=work.map(p=>({lng:p[0],lat:p[1]}));
      if(pts.length>=2){
        const a=pts[0], b=pts[pts.length-1];
        if(a.lng!==b.lng||a.lat!==b.lat) pts.push({lng:a.lng,lat:a.lat});
      }
      if(pts.length>=4) out.push({ country:c, countryKey:key||countryKey(c), kind, points:pts });
    }else{
      // stroke paths: normalize + split so no line ever crosses the whole globe
      // convert unwrapped back to display segments
      const display=work.map(p=>[p[0],p[1]]);
      const segs=splitRingForGlobePath(display);
      for(const pts of segs){
        if(pts.length>=2) out.push({ country:c, countryKey:key||countryKey(c), kind, points:pts });
      }
    }
  }
  return out;
}
/** Paint geom: real country outline (mild simplify), not bbox squares */
function paintGeometryForCountry(c, preferMicroBBox){
  if(!c) return null;
  // micro still uses real geom if available — bbox looked "wrong" next to borders
  if(c.__paintGeometry) return c.__paintGeometry;
  const src=c.__displayGeometry||c.geometry;
  if(!src) return null;
  try{
    const paths=geometryBorderPaths(src,c,c.__key,'base',0.04,{maxPts:160,maxRings:5,minRelArea:0.03,closedRings:true});
    if(!paths.length) return src;
    // MultiPolygon of simplified outer rings
    const coords=paths.map(p=>{
      const ring=p.points.map(pt=>[pt.lng,pt.lat]);
      return [ring];
    });
    c.__paintGeometry=coords.length===1
      ? {type:'Polygon',coordinates:coords[0]}
      : {type:'MultiPolygon',coordinates:coords};
    return c.__paintGeometry;
  }catch(e){}
  return src;
}
function listEsimCoverageCountries(){
  const cov=(window.__SIMJ_LAST_COVERAGE&&window.__SIMJ_LAST_COVERAGE.countries)||[];
  return cov.filter(x=>Number(x.esims||0)>0);
}
function applyBluePolygonStyle(){
  if(!state.globe) return;
  // Higher altitude + no transition = no z-fight flash on satellite tiles
  state.globe
    .polygonAltitude(d=>d.__selected?0.012:0.008)
    .polygonCapColor(d=>d.__selected?SIMJ_BLUE.selCap:(d.__esim?SIMJ_BLUE.esimCap:'rgba(56,189,248,.35)'))
    .polygonSideColor(d=>d.__selected?SIMJ_BLUE.selSide:(d.__esim?SIMJ_BLUE.esimSide:'rgba(14,116,180,.12)'))
    .polygonStrokeColor(d=>d.__selected?'rgba(186,230,253,0)':(d.__esim?'rgba(125,211,252,0)':'rgba(125,211,252,0)'))
    .polygonsTransitionDuration(0);
  try{
    const THREE=window.THREE||self.THREE;
    if(THREE&&state.globe.polygonCapMaterial){
      const cap=new THREE.MeshBasicMaterial({color:0x0ea5e9,transparent:true,opacity:0.5,depthWrite:false,depthTest:true,side:THREE.DoubleSide});
      const side=new THREE.MeshBasicMaterial({color:0x0284c7,transparent:true,opacity:0.12,depthWrite:false,depthTest:true,side:THREE.DoubleSide});
      state.globe.polygonCapMaterial(cap).polygonSideMaterial(side);
    }
  }catch(e){}
}
/** Always keep ALL eSIM countries highlighted (blue); selected is brighter blue */
function paintSelectionAndCoverage(selectedCountry){
  if(!state.globe) return;
  if(typeof window.SIMJ_PORTAL?.applyCoverage==='function' && window.__SIMJ_LAST_COVERAGE){
    try{ window.SIMJ_PORTAL.applyCoverage(window.__SIMJ_LAST_COVERAGE); return; }catch(e){}
  }
  const esimList=listEsimCoverageCountries();
  const selectedIso=String(selectedCountry?.__iso2||'').toUpperCase();
  const features=[];
  for(const item of esimList){
    const iso=String(item.iso||'').toUpperCase();
    const cc=(state.countries||[]).find(y=>String(y.__iso2||'').toUpperCase()===iso);
    if(!cc) continue;
    features.push({
      geometry:paintGeometryForCountry(cc, false),
      iso2:iso,
      name:cc.__nameZh||item.name||iso,
      __esim:true,
      __esims:Number(item.esims||0),
      __selected:!!(selectedIso&&iso===selectedIso),
    });
  }
  if(selectedCountry){
    const iso=selectedIso;
    if(!features.some(f=>f.iso2===iso)){
      features.push({
        geometry:paintGeometryForCountry(selectedCountry, false),
        iso2:iso,
        name:selectedCountry.__nameZh||iso,
        __esim:false,
        __selected:true,
      });
    }else{
      features.forEach(f=>{ if(f.iso2===iso) f.__selected=true; });
    }
  }
  try{
    const list=features.filter(f=>f.geometry);
    const sig=list.map(f=>(f.iso2||'')+':'+(f.__selected?1:0)+':'+(f.__esim?1:0)).join('|');
    if(state._polySig===sig && (state.globe.polygonsData?.()||[]).length===list.length){
      applyBluePolygonStyle();
      return;
    }
    state._polySig=sig;
    state.globe.polygonsData(list);
    applyBluePolygonStyle();
  }catch(e){ console.warn('paintSelectionAndCoverage', e); }
}
function showSelectedAnnotation(c,geometry){
  if(!c){
    if(typeof window.SIMJ_PORTAL?.applyCoverage==='function'&&window.__SIMJ_LAST_COVERAGE){
      window.SIMJ_PORTAL.applyCoverage(window.__SIMJ_LAST_COVERAGE);
    }else state.globe?.polygonsData([]);
    return;
  }
  paintSelectionAndCoverage(c);
}
function selectedBoundaryLayers(geometry,c,key){
  return geometryBorderPaths(geometry,c,key,'selected-core',0.025,{maxPts:240,maxRings:5,minRelArea:0.02});
}
function makeBorderPaths(countries,eps=0.04){
  // Clean world borders: main landmass + few major islands, antimeridian-split paths
  const out=[];
  for(const c of countries){
    out.push(...geometryBorderPaths(c.geometry,c,c.__key,'base',eps,{maxPts:160,maxRings:3,minRelArea:0.06}));
  }
  return out;
}
function extractBoundaryGeometry(data,c){if(!data)return null;if(data.type==='Feature'&&data.geometry)return data.geometry;if(data.type==='FeatureCollection'&&Array.isArray(data.features)){const key2=(c.__iso2||'').toUpperCase(),key3=(c.__iso3||'').toUpperCase();const match=data.features.find(f=>{const p=f.properties||{};return String(p.ISO_A2||p.iso_a2||p['ISO3166-1-Alpha-2']||p.shapeISO||'').toUpperCase()===key2||String(p.ISO_A3||p.iso_a3||p['ISO3166-1-Alpha-3']||'').toUpperCase()===key3})||data.features[0];return match?.geometry||null}if(data.type==='Polygon'||data.type==='MultiPolygon')return data;return null}
async function fetchJSONWithTimeout(url,ms=16000){const ctl=new AbortController(),timer=setTimeout(()=>ctl.abort(),ms);try{const r=await fetch(url,{signal:ctl.signal,headers:{Accept:'application/json, application/geo+json'}});if(!r.ok)throw new Error('HTTP '+r.status);return await r.json()}finally{clearTimeout(timer)}}
async function fetchAccurateBoundary(c){const key=c.__key||countryKey(c);if(state.accurateBoundaryCache.has(key))return state.accurateBoundaryCache.get(key);let geometry=null;const iso3=String(c.__iso3||'').toUpperCase(),iso2=String(c.__iso2||'').toLowerCase();if(/^[A-Z]{3}$/.test(iso3)&&iso3!=='XKX'){try{const meta=await fetchJSONWithTimeout(`https://www.geoboundaries.org/api/current/gbOpen/${iso3}/ADM0/`);const url=meta?.gjDownloadURL||meta?.simplifiedGeometryGeoJSON||meta?.staticDownloadLink;if(url){const data=await fetchJSONWithTimeout(url,22000);geometry=extractBoundaryGeometry(data,c)}}catch(e){console.warn('geoBoundaries unavailable',iso3,e)}}if(!geometry&&/^[a-z]{2}$/.test(iso2)){try{const url=`https://nominatim.openstreetmap.org/search?format=geojson&polygon_geojson=1&featuretype=country&countrycodes=${encodeURIComponent(iso2)}&limit=1`;const data=await fetchJSONWithTimeout(url,18000);geometry=extractBoundaryGeometry(data,c)}catch(e){console.warn('OSM boundary unavailable',iso2,e)}}if(!geometry)throw new Error('No accurate boundary');state.accurateBoundaryCache.set(key,geometry);return geometry}
// Remote "accurate boundary" fetch causes border/fill jump (geometry swap).
// Keep local GEOJSON for stable borders; only optionally refine microstates once.
const SIMJ_REFINE_MICROSTATES=false;
async function loadSelectedBoundary(c){
  const token=++state.boundaryRequestToken,key=c.__key||countryKey(c);
  const geom=c.__displayGeometry||c.geometry;
  showSelectedAnnotation(c,geom);
  state.selectedBoundaryPaths=selectedBoundaryLayers(geom,c,key);
  refreshBorderPaths();
  if(!SIMJ_REFINE_MICROSTATES||!COMPACT_MICROSTATE_ISOS.has(c.__iso2))return;
  try{
    const geometry=await fetchAccurateBoundary(c);
    if(token!==state.boundaryRequestToken||state.selected?.__key!==key)return;
    showSelectedAnnotation(c,geometry);
    state.selectedBoundaryPaths=selectedBoundaryLayers(geometry,c,key);
    refreshBorderPaths();
  }catch(e){/* keep local geometry */}
}
async function prefetchAndorraBoundary(){/* disabled: remote refine caused border flicker */}
function scheduleAccurateHoverBoundary(c){
  // Do not fetch remote borders on hover — only use local path highlight via hoveredKey
  clearTimeout(state.hoverBoundaryTimer);
  state.hoverBoundaryPaths=[];
  state.hoverBoundaryToken++;
  refreshBorderPaths();
}
function forceTileRefinement(){
  if(!state.globe||state.outlineMode||state.tileMode==='offline')return;
  // No camera pulse (was causing freezes near surface). Only nudge tile engine.
  try{
    const pov=state.globe.pointOfView();
    if(pov) state.globe.updatePov?.(pov);
  }catch(e){}
}
function updateAttribution(){const box=$('tile-attribution');if(!box)return;if(state.outlineMode){box.innerHTML='纯轮廓模式 · 离线内置地图';return}const src=TILE_SOURCES[state.tileMode]||TILE_SOURCES.offline;if(state.tileMode==='satellite')box.innerHTML='在线卫星瓦片 · '+src.attribution;if(state.tileMode==='street')box.innerHTML='在线标准地图 · <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">'+src.attribution+'</a>';if(state.tileMode==='offline')box.innerHTML=src.attribution+' · 无需网络'}
function updateSourceButton(){const btn=$('source'),span=btn?.querySelector('span');if(!btn||!span)return;const src=TILE_SOURCES[state.tileMode]||TILE_SOURCES.offline;span.textContent=src.label;btn.classList.toggle('offline',state.tileMode==='offline');btn.classList.toggle('active',state.tileMode!=='offline');btn.title=state.outlineMode?'当前为纯轮廓模式，点击切换地图源并退出轮廓模式':'切换在线卫星、在线地图或离线贴图'}
function refreshBorderPaths(){
  if(!state.globe)return;
  // While dragging: keep last GPU paths frozen (no rebuild mid rotate/zoom)
  if(state.interacting) return;
  const show=state.borders&&!state.outlineMode;
  if(!show){state.globe.pathsData([]);return}
  // Pick cached level (already built at load)
  const base=state.borderPaths||state.borderPathsLite||[];
  const selectedKey=state.selected?.__key||'';
  let paths;
  if(state.selectedBoundaryPaths.length&&selectedKey){
    // Avoid full array rebuild+sort when only selection overlay changes: filter selected base once
    paths=[];
    for(const p of base){
      if(p.countryKey===selectedKey) continue;
      paths.push(p);
    }
    for(const p of state.selectedBoundaryPaths) paths.push(p);
  }else{
    paths=base;
  }
  state.globe.pathsData(paths)
    .pathPoints('points').pathPointLat('lat').pathPointLng('lng')
    .pathPointAlt(d=>d.kind==='selected-core'?.016:.008)
    .pathColor(d=>d.kind==='selected-core'?SIMJ_BLUE.pathSel:SIMJ_BLUE.border)
    .pathStroke(d=>d.kind==='selected-core'?.0045:.0018)
    .pathTransitionDuration(0)
    .pathDashLength(1)
    .pathDashGap(0)
    .pathResolution(2);
}
function applyMapMode(force=false){
  if(!state.globe)return;
  updateSourceButton();
  updateAttribution();
  if(state.outlineMode){
    state.globe.globeTileEngineUrl(null);
    if(force)state.globe.globeTileEngineClearCache?.();
    state.activeTileMode=null;
    updateStableTexture(true);
    refreshBorderPaths();
    return;
  }
  if(state.tileMode==='offline'){
    state.globe.globeTileEngineUrl(null);
    if(force)state.globe.globeTileEngineClearCache?.();
    state.activeTileMode=null;
    updateStableTexture(true);
    refreshBorderPaths();
    return;
  }
  const src=TILE_SOURCES[state.tileMode];
  if(force||state.activeTileMode!==state.tileMode){
    state.globe.globeTileEngineClearCache?.();
    const alt=state.globe.pointOfView?.()?.altitude??1.8;
    const maxLv=simjTileMaxForAltitude(alt, state.tileMode);
    const curve=simjCurveForAltitude(alt);
    state.quality.tileMax=maxLv;
    state.quality.curve=curve;
    state.quality.lastAlt=alt;
    // transparent base + online XYZ; adaptive maxLevel for sharp zoom
    state.globe
      .globeImageUrl(TRANSPARENT_TEXTURE)
      .globeCurvatureResolution(curve)
      .globeTileEngineMaxLevel(maxLv)
      .globeTileEngineUrl((x,y,l)=>src.url(x,y,l));
    state.activeTileMode=state.tileMode;
    state.currentTexture=null;
  }
  refreshBorderPaths();
  setTimeout(forceTileRefinement,60);
  setTimeout(()=>simjApplyAdaptiveLod(true),200);
}
function setOutlineMode(v){state.outlineMode=v;const btn=$('outline');if(btn)btn.classList.toggle('active',v);outlineBackground(v);if(state.globe)state.globe.showAtmosphere(v?false:state.atmosphere);applyMapMode(true)}
function cycleTileMode(){const i=TILE_MODE_ORDER.indexOf(state.tileMode);state.tileMode=TILE_MODE_ORDER[(i+1)%TILE_MODE_ORDER.length];if(state.outlineMode){state.outlineMode=false;$('outline')?.classList.remove('active');outlineBackground(false);if(state.globe)state.globe.showAtmosphere(state.atmosphere)}applyMapMode(true)}
function makeBoundaries(countries){return countries}
function applyTextureTuning(){
  const THREE=window.THREE||self.THREE;
  if(!state.globe||!THREE)return;
  const renderer=state.globe.renderer?.();
  if(renderer){
    // keep DPR sharp
    const dpr=state.quality.dpr||simjComputeDpr();
    try{ renderer.setPixelRatio(dpr); }catch(e){}
  }
  const maxAniso=Math.min(16, renderer?.capabilities?.getMaxAnisotropy?.()||1);
  state.globe.scene?.().traverse?.(obj=>{
    const mats=Array.isArray(obj.material)?obj.material:[obj.material];
    mats.filter(Boolean).forEach(mat=>{
      if(mat.map){
        mat.map.anisotropy=maxAniso;
        mat.map.minFilter=THREE.LinearMipmapLinearFilter;
        mat.map.magFilter=THREE.LinearFilter;
        mat.map.generateMipmaps=true;
        mat.map.needsUpdate=true;
      }
      // slightly crisper materials
      if(mat.roughness!=null) mat.roughness=Math.min(mat.roughness??1, 0.92);
    });
  });
}
function updatePoints(extra=null){if(!state.globe)return;state.globe.pointsData([])}
function refreshBoundaries(){if(state.outlineMode||state.tileMode==='offline')updateStableTexture(false);refreshBorderPaths()}
function isMicroCountry(c){
  if(!c) return false;
  const iso=c.__iso2||'';
  if(COMPACT_MICROSTATE_ISOS.has(iso)||SMALL_COUNTRY_FORCE_ISOS.has(iso)||c.__isSmall) return true;
  const span=Math.max(Number(c.__latSpan)||0,Number(c.__lngSpan)||0);
  return span>0 && span<2.2;
}
function countryFlyAltitude(c){
  // Keep altitude high enough that Esri tiles stay cheap; low alt = freeze
  const span=Math.max(Number(c.__latSpan)||0,Number(c.__lngSpan)||0);
  const iso=c.__iso2||'';
  let alt=1.0;
  if(COMPACT_MICROSTATE_ISOS.has(iso)||(c.__isSmall&&span<1)) alt=0.62;
  else if(SMALL_COUNTRY_FORCE_ISOS.has(iso)||span<.65) alt=0.58;
  else if(span<1.8) alt=0.55;
  else if(span<4) alt=0.60;
  else if(span<9) alt=0.78;
  else if(span<20) alt=1.0;
  else alt=1.25;
  return Math.max(0.55, alt);
}
/** Cheap highlight geometry for microstates — avoids heavy polygon remesh freezes */
function microSelectGeometry(c){
  const lat=Number(c?.__center?.lat);
  const lng=Number(c?.__center?.lng);
  if(!Number.isFinite(lat)||!Number.isFinite(lng)) return null;
  const dlat=Math.max(0.12, Math.min(0.55, (Number(c.__latSpan)||0.2)*0.85||0.18));
  const dlng=Math.max(0.12, Math.min(0.75, (Number(c.__lngSpan)||0.2)*0.85||0.18));
  return {
    type:'Polygon',
    coordinates:[[
      [lng-dlng, lat-dlat],
      [lng+dlng, lat-dlat],
      [lng+dlng, lat+dlat],
      [lng-dlng, lat+dlat],
      [lng-dlng, lat-dlat]
    ]]
  };
}
function setRotate(v){state.rotating=v;const c=state.globe?.controls();if(c)c.autoRotate=v;$('rotate').classList.toggle('active',v)}
function getCountryMeta(c){
  const iso=String(c?.__iso2||'').toUpperCase();
  const metaTable=OFFLINE_COUNTRY_META||{};
  const specialTable=SPECIAL_COUNTRY_META||{};
  const fallback=metaTable[iso]||specialTable[c?.__nameEn]||{};
  // Always prefer packaged flag path when available
  let flag=fallback.flag||'';
  if(!flag&&iso&&iso.length===2) flag='/assets/flags/'+iso+'.png';
  const special={
    HK:{dial:'+852',length:(PHONE_LENGTH_HINTS.HK||'8位'),flag:flag||'/assets/flags/HK.png'},
    MO:{dial:'+853',length:(PHONE_LENGTH_HINTS.MO||'8位'),flag:flag||'/assets/flags/MO.png'},
    // 中国台湾省：使用打包国旗 TW.png
    TW:{dial:'+886',length:(PHONE_LENGTH_HINTS.TW||'9位'),flag:flag||'/assets/flags/TW.png'},
    AD:{dial:'+376',length:'6位 / 9位',flag:flag||'/assets/flags/AD.png'},
    CN:{dial:'+86',length:(PHONE_LENGTH_HINTS.CN||'11位'),flag:flag||'/assets/flags/CN.png'},
  };
  const base=special[iso]||fallback;
  return{
    dial:base.dial||fallback.dial||'—',
    length:base.length||PHONE_LENGTH_HINTS[iso]||'暂无统一数据',
    flag:base.flag||flag||''
  };
}
function applyCountryMeta(c){
  const meta=getCountryMeta(c);
  $('country-dial').textContent=meta.dial||'—';
  $('country-length').textContent=meta.length||'—';
  let flag=meta.flag||'';
  if(flag&&flag.startsWith('/')) flag=flag; // same-origin static
  const icon=$('country-flag');
  const bg=$('info-flag-bg');
  if(!icon||!bg)return;
  if(flag){
    const cssUrl='url("'+String(flag).replace(/"/g,'')+'")';
    icon.textContent='';
    icon.style.backgroundImage=cssUrl;
    icon.style.backgroundSize='cover';
    icon.style.backgroundPosition='center';
    icon.style.backgroundRepeat='no-repeat';
    bg.style.backgroundImage=cssUrl;
    bg.style.backgroundSize='cover';
    bg.style.backgroundPosition='center';
    // preload to surface broken paths
    const img=new Image();
    img.onload=()=>{icon.style.backgroundImage=cssUrl;bg.style.backgroundImage=cssUrl;};
    img.onerror=()=>{
      // fallback: try uppercase iso path once
      const iso=String(c?.__iso2||'').toUpperCase();
      if(iso&&!String(flag).endsWith('/'+iso+'.png')){
        const alt='/assets/flags/'+iso+'.png';
        icon.style.backgroundImage='url("'+alt+'")';
        bg.style.backgroundImage='url("'+alt+'")';
      }
    };
    img.src=flag;
  }else{
    icon.textContent='';
    icon.style.backgroundImage='none';
    bg.style.backgroundImage='none';
  }
}
function selectCountry(c,fly=true){
  if(!c||!state.globe)return;
  try{
    const p=c.__center||{lat:20,lng:10};
    if(!Number.isFinite(p.lat)||!Number.isFinite(p.lng)){ p.lat=20; p.lng=10; }
    if(state._flyTimer){ clearTimeout(state._flyTimer); state._flyTimer=0; }
    if(state._gfxTimer){ clearTimeout(state._gfxTimer); state._gfxTimer=0; }
    if(state._lodTimer2){ clearTimeout(state._lodTimer2); state._lodTimer2=0; }

    const micro=isMicroCountry(c);
    state.selected=c;
    state.hovered=null;
    state.hoverBoundaryToken++;
    clearTimeout(state.hoverBoundaryTimer);
    state.hoverBoundaryPaths=[];
    state.selectedBoundaryPaths=[];
    state.quality.microHoldUntil=Date.now()+(micro?3000:1200);

    // 1) DOM panel
    setRotate(false);
    $('country-name').textContent=c.__nameZh||'—';
    $('country-en').textContent=c.__nameEn||'—';
    $('country-iso').textContent=c.__iso3||c.__iso2||'—';
    $('country-chip').textContent=c.__iso2||c.__iso3||'—';
    $('country-coord').textContent=`${Number(p.lat).toFixed(2)}°, ${Number(p.lng).toFixed(2)}°`;
    try{ applyCountryMeta(c); }catch(e){}
    $('info').classList.add('show');
    $('results').classList.remove('show');
    if($('search')) $('search').value=c.__nameZh||'';
    window.SIMJ_GET_SELECTED_ISO=()=>String(state.selected?.__iso2||'').toUpperCase();

    // Single stable paint (no double rebuild mid-flight → no fill flash)
    try{
      paintSelectionAndCoverage(c);
      const geom=paintGeometryForCountry(c,false)||c.geometry;
      state.selectedBoundaryPaths=selectedBoundaryLayers(geom,c,c.__key||countryKey(c));
      refreshBorderPaths();
    }catch(e){}

    const finishSelectionGfx=()=>{
      try{
        state.interacting=false;
        simjSetRendererDpr(Math.min(state.quality.dpr||1.3, 1.25));
        try{ state.globe.globeTileEngineMaxLevel(micro?11:12); state.quality.tileMax=micro?11:12; }catch(e){}
        // paths already set; only soft re-apply style without geometry swap
        state._gfxTimer=setTimeout(()=>{
          try{
            if(state.selected!==c) return;
            applyBluePolygonStyle();
          }catch(e){ console.warn('paint after select', e); }
        }, 40);
      }catch(e){ console.warn('finishSelectionGfx', e); state.interacting=false; }
    };

    const alt=countryFlyAltitude(c);
    if(fly){
      // Smooth camera — do NOT clear borders mid-flight (that made lines flash/mess)
      state.interacting=true;
      simjSetRendererDpr(1.05);
      hideTip();
      try{ state.globe.globeTileEngineMaxLevel(10); state.quality.tileMax=10; }catch(e){}
      // Silky fly: longer duration; micro still animated (not 0ms)
      const flyMs=micro?560:780;
      try{ state.globe.pointOfView({lat:p.lat,lng:p.lng,altitude:alt}, flyMs); }
      catch(e){ console.warn('fly',e); }
      state._flyTimer=setTimeout(finishSelectionGfx, flyMs+30);
    }else{
      finishSelectionGfx();
    }
  }catch(err){
    console.error('selectCountry failed', err);
    state.interacting=false;
  }
}
window.SIMJ_GET_SELECTED_ISO=()=>String((typeof state!=='undefined'&&state.selected&&state.selected.__iso2)||'').toUpperCase();
function clearSelection(){
  state.selected=null;
  state._polySig='';
  state.selectedBoundaryPaths=[];
  state.hoverBoundaryPaths=[];
  state.boundaryRequestToken++;
  state.hoverBoundaryToken++;
  clearTimeout(state.hoverBoundaryTimer);
  window.SIMJ_GET_SELECTED_ISO=()=>'';
  // Restore eSIM highlights for ALL eSIM countries (no full border rebuild)
  paintSelectionAndCoverage(null);
  updatePoints();
  $('country-dial').textContent='—';
  $('country-length').textContent='—';
  $('country-chip').textContent='—';
  $('country-flag').textContent='';
  $('country-flag').style.backgroundImage='none';
  $('info-flag-bg').style.backgroundImage='none';
  $('info').classList.remove('show');
}
function renderResults(q){const box=$('results');q=q.trim().toLowerCase();state.searchIndex=-1;if(!q){box.classList.remove('show');box.innerHTML='';return}state.filtered=state.countries.filter(c=>c.__search.includes(q)).slice(0,10);box.innerHTML=state.filtered.length?state.filtered.map((c,i)=>`<button class="result" data-i="${i}"><span><strong>${esc(c.__nameZh)}</strong><br><small>${esc(c.__nameEn)}</small></span><small>${esc(c.__iso3||c.__iso2)}</small></button>`).join(''):'<div style="padding:12px;color:#8292aa;font-size:12px">没有找到匹配国家</div>';box.classList.add('show');box.querySelectorAll('.result').forEach(b=>b.onclick=()=>selectCountry(state.filtered[+b.dataset.i]))}
function updateSearchActive(){$('results').querySelectorAll('.result').forEach((n,i)=>n.classList.toggle('active',i===state.searchIndex))}
function pointerCoords(ev){const r=$('globe').getBoundingClientRect();return state.globe?.toGlobeCoords(ev.clientX-r.left,ev.clientY-r.top)||null}
function hideTip(){$('hover-tip').style.display='none'}
function updateHover(ev){
  // Skip hit-test while rotating/zooming; hover never rebuilds borders (tip only)
  if(!state.globe||state.interacting||state.pointerDown?.dragged) return;
  const coords=pointerCoords(ev),c=countryAt(coords);
  state.hovered=c;
  const tip=$('hover-tip');
  if(!c){hideTip();if(!state.interacting)document.body.style.cursor='default';return}
  document.body.style.cursor='pointer';
  tip.innerHTML=`<b>${esc(c.__nameZh)}</b><br><small>${esc(c.__nameEn)} · ${esc(c.__iso3||c.__iso2)}</small>`;
  tip.style.display='block';
  const w=tip.offsetWidth,h=tip.offsetHeight;
  tip.style.left=Math.min(ev.clientX+14,innerWidth-w-10)+'px';
  tip.style.top=Math.min(ev.clientY+14,innerHeight-h-10)+'px';
}
function scheduleHover(ev){
  if(state.interacting) return;
  state.lastPointer={clientX:ev.clientX,clientY:ev.clientY};
  if(state.raf)return;
  // ~8fps hover is enough for tips; avoids countryAt on every move
  state.raf=requestAnimationFrame(()=>{
    state.raf=0;
    if(state.quality._hoverSkip){ state.quality._hoverSkip=false; return; }
    state.quality._hoverSkip=true;
    if(state.quality._hoverSkip2){ state.quality._hoverSkip2=false; return; }
    state.quality._hoverSkip2=true;
    updateHover(state.lastPointer);
  });
}
function bindUI(){$('search').addEventListener('input',e=>renderResults(e.target.value));$('search').addEventListener('keydown',e=>{if(e.key==='ArrowDown'&&state.filtered.length){e.preventDefault();state.searchIndex=Math.min(state.searchIndex+1,state.filtered.length-1);updateSearchActive()}else if(e.key==='ArrowUp'&&state.filtered.length){e.preventDefault();state.searchIndex=Math.max(state.searchIndex-1,0);updateSearchActive()}else if(e.key==='Enter'&&state.filtered.length){e.preventDefault();selectCountry(state.filtered[Math.max(0,state.searchIndex)])}else if(e.key==='Escape'){$('results').classList.remove('show');e.target.blur()}});document.addEventListener('pointerdown',e=>{if(!e.target.closest('.search-wrap'))$('results').classList.remove('show')});$('rotate').onclick=()=>setRotate(!state.rotating);$('source').onclick=cycleTileMode;$('borders').onclick=()=>{state.borders=!state.borders;$('borders').classList.toggle('active',state.borders);refreshBoundaries()};$('atmos').onclick=()=>{state.atmosphere=!state.atmosphere;$('atmos').classList.toggle('active',state.atmosphere);if(!state.outlineMode)state.globe.showAtmosphere(state.atmosphere)};$('outline').onclick=()=>setOutlineMode(!state.outlineMode);$('reset').onclick=()=>{clearSelection();state.globe.pointOfView({lat:20,lng:10,altitude:1.82},850);setTimeout(forceTileRefinement,950)};$('close').onclick=clearSelection;$('full').onclick=async()=>{try{document.fullscreenElement?await document.exitFullscreen():await document.documentElement.requestFullscreen()}catch(e){}};addEventListener('resize',()=>{state.globe?.width(innerWidth).height(innerHeight);const renderer=state.globe?.renderer?.();if(renderer){renderer.setPixelRatio(Math.min(window.devicePixelRatio||1,4));renderer.setSize(innerWidth,innerHeight,false)}applyTextureTuning();setTimeout(forceTileRefinement,80)});addEventListener('online',()=>updateAttribution());addEventListener('offline',()=>{const box=$('tile-attribution');if(box)box.innerHTML='网络已断开 · 可点击地图源切换到离线模式'});const target=$('globe');target.addEventListener('pointermove',e=>{if(state.pointerDown){const dx=e.clientX-state.pointerDown.x,dy=e.clientY-state.pointerDown.y;if(Math.hypot(dx,dy)>6){state.pointerDown.dragged=true;hideTip()}}scheduleHover(e)});target.addEventListener('pointerleave',()=>{state.hovered=null;hideTip();document.body.style.cursor='default'});target.addEventListener('pointerdown',e=>{if(e.button===0)state.pointerDown={x:e.clientX,y:e.clientY,dragged:false}});target.addEventListener('pointerup',e=>{const d=state.pointerDown;state.pointerDown=null;if(!d||d.dragged||e.button!==0)return;const coords=pointerCoords(e),c=countryAt(coords);if(c)selectCountry(c);else{clearSelection();if(coords)$('country-coord').textContent=`${coords.lat.toFixed(2)}°, ${coords.lng.toFixed(2)}°`}})}

async function simjLoadData(){
  simjSetStatus('加载国家边界与资料（VPS 静态文件，首次后走浏览器缓存）…');
  const jobs = [
    simjFetchJson('/data/countries.geojson').then(j=>{ GEOJSON=j; }),
    simjFetchJson('/data/offline-country-meta.json').then(j=>{ OFFLINE_COUNTRY_META=j; }),
    simjFetchJson('/data/special-country-meta.json').then(j=>{ SPECIAL_COUNTRY_META=j; }),
    simjFetchJson('/data/micro-features.json').then(j=>{ MICRO_FEATURES=j; }).catch(()=>{ MICRO_FEATURES=[]; }),
  ];
  await Promise.all(jobs);
}

function simjPreloadOfflineTextures(){
  // full offline/outline textures stay available — prefetch after first paint
  ;[EARTH_TEXTURE, EARTH_BORDER_TEXTURE, OUTLINE_PLAIN_TEXTURE, OUTLINE_BORDER_TEXTURE].forEach(function(url){
    if(!url || String(url).indexOf('/assets/')!==0) return;
    try{ var img=new Image(); img.decoding='async'; img.src=url; }catch(e){}
  });
}

async function start(){
  try{
    simjSetStatus('创建地球并连接在线卫星瓦片…');
    var GlobeFn = (typeof Globe==='function') ? Globe : window.Globe;
    if(typeof GlobeFn!=='function') throw new Error('Globe engine missing');
    window.Globe = GlobeFn;

    bindUI();
    outlineBackground(false);
    // Quality profile: sharp on hiDPI, still fast on low-end
    state.quality.dpr=simjComputeDpr();
    state.quality.aa=!simjIsLowEnd();
    state.currentTexture=TRANSPARENT_TEXTURE;
    const startCurve=simjCurveForAltitude(1.82);
    state.quality.curve=startCurve;
    state.globe=GlobeFn({
      animateIn:true,
      rendererConfig:{
        antialias:state.quality.aa,
        alpha:true,
        powerPreference:'high-performance',
        // preserveDrawingBuffer false = faster
        preserveDrawingBuffer:false
      }
    })($('globe'))
      .width(innerWidth).height(innerHeight).backgroundColor('rgba(0,0,0,0)')
      .globeImageUrl(TRANSPARENT_TEXTURE)
      .globeCurvatureResolution(startCurve)
      .showAtmosphere(true).atmosphereColor('#5bd7ff').atmosphereAltitude(.15)
      .enablePointerInteraction(false).showPointerCursor(false)
      .polygonsData([]).polygonGeoJsonGeometry(d=>d.geometry)
      .polygonCapColor(()=> 'rgba(43,112,238,.46)').polygonSideColor(()=> 'rgba(22,67,157,.20)')
      .polygonStrokeColor(()=> 'rgba(49,152,255,0)').polygonAltitude(.008)
      .polygonCapCurvatureResolution(.35).polygonsTransitionDuration(0)
      .pathsData([]).pathPoints('points').pathPointLat('lat').pathPointLng('lng').pathPointAlt(.006).pathStroke(null).pathTransitionDuration(0)
      .pointsData([]).pointLat('lat').pointLng('lng').pointColor(()=> '#78e7ff').pointRadius(.32).pointAltitude(.025).pointLabel('name');
    const renderer=state.globe.renderer?.();
    if(renderer){
      renderer.setPixelRatio(state.quality.dpr);
      renderer.toneMappingExposure=1.08;
      renderer.setSize(innerWidth,innerHeight,false);
      // three r152+ : better color
      try{ if(renderer.outputColorSpace!=null) renderer.outputColorSpace = (window.THREE&&THREE.SRGBColorSpace)||renderer.outputColorSpace; }catch(e){}
    }
    applyTextureTuning();
    applyMapMode(true);
    const controls=state.globe.controls();
    controls.autoRotate=true;controls.autoRotateSpeed=.28;controls.enableDamping=true;controls.dampingFactor=.12;
    // Prevent ultra-close zoom (tile storm / freezes)
    controls.minDistance=118;controls.maxDistance=650;
    controls.rotateSpeed=0.68;controls.zoomSpeed=0.75;
    controls.addEventListener('start',()=>{
      if(state.rotating)setRotate(false);
      simjBeginInteract();
    });
    // Do NOT run LOD on every change frame — that is the main stutter source
    controls.addEventListener('end',()=>{ simjEndInteract(); });
    // Wheel zoom may not always fire controls start/end consistently — cover it
    const globeEl=$('globe');
    if(globeEl){
      let wheelTimer=0;
      globeEl.addEventListener('wheel',()=>{
        if(!state.interacting) simjBeginInteract();
        clearTimeout(wheelTimer);
        // Longer settle so continuous far→near zoom does not thrash tiles/paths
        wheelTimer=setTimeout(()=>{ simjEndInteract(); }, 240);
      },{passive:true});
    }
    state.globe.pointOfView({lat:20,lng:10,altitude:1.82},0);
    // Initial LOD once after paint
    setTimeout(()=>{ try{ simjApplyAdaptiveLod(true); }catch(e){} }, 200);
    setTimeout(()=>{ try{ forceTileRefinement(); }catch(e){} }, 600);
    // first paint ready — user sees online globe now
    simjHideStatus();
    simjPreloadOfflineTextures();

    // 2) Full borders / meta / hit-test (same features as original HTML)
    await simjLoadData();
    state.countries=prepareCountries(GEOJSON.features||[]);
    state.boundaries=makeBoundaries(state.countries);
    // Clean borders: unwrap + major islands + mild RDP (not ultra-chunky)
    state.borderPathsLite=makeBorderPaths(state.countries, 0.04);
    state.borderPathsDetail=state.borderPathsLite;
    state.borderLod='lite';
    state.borderPaths=state.borderPathsLite;
    state.countryGrid=buildCountryGrid(state.countries);
    state.smallCountryPoints=buildSmallCountryPoints(state.countries);
    // Prebuild cheap paint geoms for faster eSIM/select fills
    for(const c of state.countries){ try{ paintGeometryForCountry(c,false); }catch(e){} }
    if(!state.countries.length) throw new Error('country borders empty');
    try{
      const pts=(state.borderPathsLite||[]).reduce((s,p)=>s+(p.points?p.points.length:0),0);
      console.info('[SIMJ] border lite paths', (state.borderPathsLite||[]).length, 'points', pts);
    }catch(e){}
    refreshBorderPaths();
    updatePoints();
    prefetchAndorraBoundary();
    try{
      window.SIMJ_GLOBE_STATE={globe:state.globe,countries:state.countries||[],get interacting(){return !!state.interacting;}};
      window.SIMJ_SELECT_COUNTRY=(typeof selectCountry==='function')?selectCountry:null;
      window.SIMJ_GLOBE_API={
        selectCountry,
        microSelectGeometry,
        paintSelectionAndCoverage,
        isMicroCountry,
      };
      window.dispatchEvent(new CustomEvent('simj-globe-ready',{detail:{globe:state.globe,countries:state.countries||[],selectCountry:window.SIMJ_SELECT_COUNTRY}}));
      // re-apply eSIM coverage if portal already loaded it
      if(window.__SIMJ_LAST_COVERAGE && typeof window.SIMJ_PORTAL?.applyCoverage==='function'){
        setTimeout(()=>{ try{ window.SIMJ_PORTAL.applyCoverage(window.__SIMJ_LAST_COVERAGE); }catch(e){} }, 100);
      }
    }catch(e){console.warn('simj bridge',e)}
  }catch(err){
    console.error(err);
    const st=$('status');
    if(st){st.classList.add('error');st.classList.remove('hidden');st.querySelector('strong').textContent='Load failed';}
    const t=$('status-text'); if(t) t.textContent=err&&err.message?err.message:String(err);
  }
}

start();
try{
  window.SIMJ_GLOBE_STATE={globe:state.globe,countries:state.countries||[]};
  window.SIMJ_SELECT_COUNTRY=(typeof selectCountry==='function')?selectCountry:null;
  window.dispatchEvent(new CustomEvent('simj-globe-ready',{detail:{globe:state.globe,countries:state.countries||[],selectCountry:window.SIMJ_SELECT_COUNTRY}}));
}catch(e){console.warn('simj bridge',e)}
})();