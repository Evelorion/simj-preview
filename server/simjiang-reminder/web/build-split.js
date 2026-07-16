/**
 * Split the monolithic full-feature globe HTML into:
 *   - slim index.html (shell, loads once, tiny)
 *   - vendor/globe.gl.js (engine, long-cache)
 *   - app/globe-app.js (all original logic, long-cache)
 *   - assets/* (textures / flags)
 *   - data/* (geojson + meta)
 *
 * Goal: keep 100% features; first paint fast via parallel static files + gzip + browser cache.
 * Files live ON the VPS; browser still downloads once, then reuses disk cache (feels local).
 */
const fs = require("fs");
const path = require("path");

const WEB = __dirname;
const SRC = path.join(WEB, "index.html");
const outDirs = ["vendor", "app", "assets", "assets/flags", "data"].map((d) =>
  path.join(WEB, d)
);
for (const d of outDirs) fs.mkdirSync(d, { recursive: true });

function decodeDataUrl(dataUrl) {
  const m = String(dataUrl).match(/^data:([^;,]+)?(;base64)?,(.*)$/s);
  if (!m) return null;
  const mime = m[1] || "application/octet-stream";
  const isB64 = !!m[2];
  const payload = m[3];
  const buf = isB64
    ? Buffer.from(payload, "base64")
    : Buffer.from(decodeURIComponent(payload), "utf8");
  return { mime, buf };
}

function extForMime(mime) {
  if (mime.includes("jpeg") || mime.includes("jpg")) return ".jpg";
  if (mime.includes("png")) return ".png";
  if (mime.includes("webp")) return ".webp";
  return ".bin";
}

console.log("Reading source...", (fs.statSync(SRC).size / 1e6).toFixed(1), "MB");
const lines = fs.readFileSync(SRC, "utf8").split(/\n/);

// ---- extract HTML shell (lines 1-23 body chrome) + styles ----
const htmlHead = [];
const bodyChrome = [];
let i = 0;
for (; i < lines.length; i++) {
  if (lines[i].startsWith("<script")) break;
  htmlHead.push(lines[i]);
}
// htmlHead is through status div, before first script

// ---- extract vendor (first script block: globe.gl) lines 24-44 ----
// L24: <script>...
// L25-43: code
// L44: </script>
let vendorStart = -1,
  vendorEnd = -1;
for (let k = 0; k < lines.length; k++) {
  if (lines[k].includes("globe.gl") && lines[k].includes("<script")) vendorStart = k;
  if (vendorStart >= 0 && lines[k].trim() === "</script>" && vendorEnd < 0) {
    vendorEnd = k;
    break;
  }
}
if (vendorStart < 0 || vendorEnd < 0) throw new Error("vendor script block not found");

const vendorBody = lines.slice(vendorStart + 1, vendorEnd).join("\n");
const vendorWrapped =
  `/* globe.gl full bundle — served as static file from VPS */\n` +
  `(function(root){\n` +
  `  var _module = (typeof module!=='undefined') ? module : undefined;\n` +
  `  var _exports = (typeof exports!=='undefined') ? exports : undefined;\n` +
  `  var _define = (typeof define!=='undefined') ? define : undefined;\n` +
  `  try { module = undefined; } catch(e) {}\n` +
  `  try { exports = undefined; } catch(e) {}\n` +
  `  try { define = undefined; } catch(e) {}\n` +
  `  try {\n` +
  vendorBody +
  `\n  } catch (err) { console.error('vendor-globe', err); }\n` +
  `  try { if (_module !== undefined) module = _module; } catch(e) {}\n` +
  `  try { if (_exports !== undefined) exports = _exports; } catch(e) {}\n` +
  `  try { if (_define !== undefined) define = _define; } catch(e) {}\n` +
  `  var G = (typeof Globe==='function') ? Globe : (root && root.Globe);\n` +
  `  if (typeof G !== 'function' && _module && _module.exports) G = _module.exports;\n` +
  `  if (typeof G === 'function') { root.Globe = G; if (typeof window!=='undefined') window.Globe = G; }\n` +
  `})(typeof globalThis!=='undefined' ? globalThis : (typeof window!=='undefined' ? window : this));\n`;
fs.writeFileSync(path.join(WEB, "vendor", "globe.gl.js"), vendorWrapped);
console.log("vendor/globe.gl.js", (vendorWrapped.length / 1e6).toFixed(2), "MB");

// ---- extract app script (second script) ----
let appStart = -1,
  appEnd = -1;
for (let k = vendorEnd + 1; k < lines.length; k++) {
  if (lines[k].trim() === "<script>" && appStart < 0) appStart = k;
  if (appStart >= 0 && lines[k].trim() === "</script>") {
    appEnd = k;
    break;
  }
}
if (appStart < 0 || appEnd < 0) throw new Error("app script block not found");
const appLines = lines.slice(appStart + 1, appEnd);

// asset replacements
const assetMap = {}; // constName -> url

function extractConstDataUrl(line, constName, fileBase) {
  const re = new RegExp(`^const ${constName}='(data:[^']+)'`);
  const m = line.match(re);
  if (!m) return null;
  const decoded = decodeDataUrl(m[1]);
  if (!decoded) return null;
  const ext = extForMime(decoded.mime);
  const rel = `assets/${fileBase}${ext}`;
  fs.writeFileSync(path.join(WEB, rel), decoded.buf);
  assetMap[constName] = "/" + rel.replace(/\\/g, "/");
  console.log("  asset", constName, "->", rel, (decoded.buf.length / 1e6).toFixed(2), "MB");
  return `const ${constName}=${JSON.stringify(assetMap[constName])};`;
}

function extractGeojson(line) {
  if (!line.startsWith("const GEOJSON=")) return null;
  const json = line.slice("const GEOJSON=".length).replace(/;?\s*$/, "");
  // validate
  JSON.parse(json);
  fs.writeFileSync(path.join(WEB, "data", "countries.geojson"), json);
  console.log("  data/countries.geojson", (json.length / 1e6).toFixed(2), "MB");
  return "let GEOJSON=null; /* loaded from /data/countries.geojson */";
}

function extractMetaObject(line, constName, fileName, extractFlags) {
  if (!line.startsWith(`const ${constName}=`)) return null;
  let json = line.slice(`const ${constName}=`.length).replace(/;?\s*$/, "");
  let obj = JSON.parse(json);
  if (extractFlags) {
    let n = 0;
    for (const [key, val] of Object.entries(obj)) {
      if (val && typeof val.flag === "string" && val.flag.startsWith("data:")) {
        const decoded = decodeDataUrl(val.flag);
        if (decoded) {
          const safe = String(key).replace(/[^A-Za-z0-9_-]/g, "_");
          const ext = extForMime(decoded.mime);
          const rel = `assets/flags/${safe}${ext}`;
          fs.writeFileSync(path.join(WEB, rel), decoded.buf);
          val.flag = "/" + rel.replace(/\\/g, "/");
          n++;
        }
      }
    }
    console.log("  flags extracted from", constName, n);
  }
  const outJson = JSON.stringify(obj);
  fs.writeFileSync(path.join(WEB, "data", fileName), outJson);
  console.log("  data/" + fileName, (outJson.length / 1e6).toFixed(2), "MB");
  return `let ${constName}=null; /* loaded from /data/${fileName} */`;
}

function extractMicroFeatures(line) {
  if (!line.startsWith("const MICRO_FEATURES=")) return null;
  const json = line.slice("const MICRO_FEATURES=".length).replace(/;?\s*$/, "");
  JSON.parse(json);
  fs.writeFileSync(path.join(WEB, "data", "micro-features.json"), json);
  console.log("  data/micro-features.json", (json.length / 1e3).toFixed(1), "KB");
  return "let MICRO_FEATURES=[]; /* loaded from /data/micro-features.json */";
}

const outApp = [];
outApp.push("/* SIMJ full globe app — static file on VPS, cached by browser */");
outApp.push("(()=>{");
outApp.push("'use strict';");
outApp.push("");
outApp.push("// ---- progressive asset loader (files already on VPS) ----");
outApp.push("function simjFetchJson(url){return fetch(url,{cache:'force-cache'}).then(r=>{if(!r.ok)throw new Error(url+' '+r.status);return r.json();});}");
outApp.push("function simjSetStatus(msg){const el=document.getElementById('status-text');if(el)el.textContent=msg||'';}");
outApp.push("function simjHideStatus(){const el=document.getElementById('status');if(el)el.classList.add('hidden');}");
outApp.push("");

for (const line of appLines) {
  let replaced = null;
  replaced =
    extractConstDataUrl(line, "EARTH_TEXTURE", "earth") ||
    extractConstDataUrl(line, "EARTH_BORDER_TEXTURE", "earth-border") ||
    extractConstDataUrl(line, "OUTLINE_PLAIN_TEXTURE", "outline-plain") ||
    extractConstDataUrl(line, "OUTLINE_BORDER_TEXTURE", "outline-border") ||
    extractConstDataUrl(line, "TRANSPARENT_TEXTURE", "transparent") ||
    extractConstDataUrl(line, "UNUSED_NIGHT_TEXTURE", "unused-night") ||
    extractConstDataUrl(line, "UNUSED_LIGHTS_TEXTURE", "unused-lights") ||
    extractConstDataUrl(line, "CLOUD_TEXTURE", "cloud") ||
    extractGeojson(line) ||
    extractMetaObject(line, "OFFLINE_COUNTRY_META", "offline-country-meta.json", true) ||
    extractMetaObject(line, "SPECIAL_COUNTRY_META", "special-country-meta.json", true) ||
    extractMicroFeatures(line);

  if (replaced) {
    outApp.push(replaced);
    continue;
  }

  // skip IIFE wrappers already present
  if (line.trim() === "(()=>{" || line.trim() === "'use strict';" || line.trim() === "})();") {
    continue;
  }

  // replace start() with progressive version that loads data first then runs original pipeline
  if (line.startsWith("function start(){")) {
    outApp.push(`
async function simjLoadData(){
  simjSetStatus('Loading country borders + meta from VPS (cached after first visit)…');
  const jobs = [
    simjFetchJson('/data/countries.geojson').then(j=>{ GEOJSON=j; }),
    simjFetchJson('/data/offline-country-meta.json').then(j=>{ OFFLINE_COUNTRY_META=j; }),
    simjFetchJson('/data/special-country-meta.json').then(j=>{ SPECIAL_COUNTRY_META=j; }),
    simjFetchJson('/data/micro-features.json').then(j=>{ MICRO_FEATURES=j; }).catch(()=>{ MICRO_FEATURES=[]; }),
  ];
  await Promise.all(jobs);
}

async function start(){
  try{
    simjSetStatus('Loading 3D engine + online tiles…');
    if(typeof Globe!=='function' && typeof window.Globe==='function'){ window.Globe=window.Globe; }
    var GlobeFn = (typeof Globe==='function') ? Globe : window.Globe;
    if(typeof GlobeFn!=='function') throw new Error('Globe engine missing');
    // expose for original code that calls Globe(...)
    window.Globe = GlobeFn;

    await simjLoadData();
    simjSetStatus('Building globe (full features)…');

    bindUI();
    state.countries=prepareCountries(GEOJSON.features||[]);
    state.boundaries=makeBoundaries(state.countries);
    state.borderPaths=makeBorderPaths(state.countries);
    state.smallCountryPoints=buildSmallCountryPoints(state.countries);
    if(!state.countries.length) throw new Error('country borders empty');
    outlineBackground(false);
    state.currentTexture=EARTH_BORDER_TEXTURE;
    state.globe=GlobeFn({animateIn:true,rendererConfig:{antialias:true,alpha:true,powerPreference:'high-performance'}})($('globe'))
      .width(innerWidth).height(innerHeight).backgroundColor('rgba(0,0,0,0)')
      .globeImageUrl(EARTH_BORDER_TEXTURE).globeCurvatureResolution(.65)
      .showAtmosphere(true).atmosphereColor('#5bd7ff').atmosphereAltitude(.17)
      .enablePointerInteraction(false).showPointerCursor(false)
      .polygonsData([]).polygonGeoJsonGeometry(d=>d.geometry)
      .polygonCapColor(()=> 'rgba(43,112,238,.46)').polygonSideColor(()=> 'rgba(22,67,157,.20)')
      .polygonStrokeColor(()=> 'rgba(49,152,255,1)').polygonAltitude(.00013)
      .polygonCapCurvatureResolution(.45).polygonsTransitionDuration(0)
      .pathsData([]).pathPoints('points').pathPointLat('lat').pathPointLng('lng').pathPointAlt(.00028).pathStroke(null).pathTransitionDuration(0)
      .pointsData([]).pointLat('lat').pointLng('lng').pointColor(()=> '#78e7ff').pointRadius(.32).pointAltitude(.025).pointLabel('name');
    const renderer=state.globe.renderer?.();
    if(renderer){renderer.setPixelRatio(Math.min(window.devicePixelRatio||1,2));renderer.toneMappingExposure=1.04;renderer.setSize(innerWidth,innerHeight,false)}
    applyTextureTuning();
    applyMapMode(true);
    prefetchAndorraBoundary();
    const controls=state.globe.controls();
    controls.autoRotate=true;controls.autoRotateSpeed=.36;controls.enableDamping=true;controls.dampingFactor=.08;
    controls.minDistance=102.5;controls.maxDistance=650;
    controls.addEventListener('start',()=>{if(state.rotating)setRotate(false)});
    state.globe.pointOfView({lat:20,lng:10,altitude:1.82},0);
    setTimeout(forceTileRefinement,120);
    setTimeout(forceTileRefinement,900);
    try{
      window.SIMJ_GLOBE_STATE={globe:state.globe,countries:state.countries||[]};
      window.SIMJ_SELECT_COUNTRY=(typeof selectCountry==='function')?selectCountry:null;
      window.dispatchEvent(new CustomEvent('simj-globe-ready',{detail:{globe:state.globe,countries:state.countries||[],selectCountry:window.SIMJ_SELECT_COUNTRY}}));
    }catch(e){console.warn('simj bridge',e)}
    simjHideStatus();
  }catch(err){
    console.error(err);
    const st=$('status');
    if(st){st.classList.add('error');st.querySelector('strong').textContent='Load failed';}
    const t=$('status-text'); if(t) t.textContent=err&&err.message?err.message:String(err);
  }
}
`.replace(/\n{3,}/g, "\n"));
    continue;
  }

  // original calls start() at end — keep
  outApp.push(line);
}

// ensure OUTLINE_TEXTURE alias works after URL load
// already `const OUTLINE_TEXTURE=OUTLINE_BORDER_TEXTURE` in source

outApp.push("})();");
const appJs = outApp.join("\n");
fs.writeFileSync(path.join(WEB, "app", "globe-app.js"), appJs);
console.log("app/globe-app.js", (appJs.length / 1e6).toFixed(2), "MB");

// ---- slim index.html ----
// Keep original HTML chrome from lines 1..23
const shell = lines.slice(0, 23); // through status
// fix status text
for (let k = 0; k < shell.length; k++) {
  if (shell[k].includes('id="status-text"')) {
    shell[k] =
      '<div id="status" class="status glass"><div class="spinner"></div><strong>正在加载 3D 地球</strong><div id="status-text">从服务器加载静态资源（首次下载，之后浏览器缓存）…</div></div>';
  }
}

const indexHtml = `${shell.join("\n")}
<link rel="preconnect" href="https://server.arcgisonline.com" crossorigin>
<script>
// progressive loader: static files already packaged on VPS
(function(){
  function setMsg(m){var el=document.getElementById('status-text'); if(el) el.textContent=m;}
  function loadScript(src){
    return new Promise(function(resolve,reject){
      var s=document.createElement('script');
      s.src=src; s.async=false;
      s.onload=function(){resolve(src);};
      s.onerror=function(){reject(new Error('fail '+src));};
      document.head.appendChild(s);
    });
  }
  async function boot(){
    try{
      setMsg('1/2 加载 3D 引擎（VPS 静态文件 / 浏览器缓存）…');
      await loadScript('/vendor/globe.gl.js?v=split1');
      if(typeof Globe!=='function' && window.Globe) { /* ok */ }
      if(typeof Globe!=='function' && typeof window.Globe!=='function'){
        throw new Error('Globe engine missing after /vendor/globe.gl.js');
      }
      setMsg('2/2 加载完整地球业务逻辑…');
      await loadScript('/app/globe-app.js?v=split1');
      // globe-app calls start() itself
    }catch(e){
      console.error(e);
      var st=document.getElementById('status');
      if(st){st.classList.add('error'); st.querySelector('strong').textContent='加载失败';}
      setMsg((e&&e.message)||String(e));
    }
  }
  boot();
})();
</script>
<script src="/simj-portal.js?v=split1"></script>
</body></html>
`;

// If shell already closed body incorrectly, fix: original line 23 is status only, body not closed
// shell lines 1-23 don't include </body>
fs.writeFileSync(path.join(WEB, "index.html"), indexHtml, "utf8");
console.log("index.html", (fs.statSync(path.join(WEB, "index.html")).size / 1024).toFixed(1), "KB");

// keep a copy of monolithic as backup if not present
console.log("DONE split build");

// size summary
function walk(dir, acc = []) {
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name);
    const st = fs.statSync(p);
    if (st.isDirectory()) walk(p, acc);
    else acc.push({ p: path.relative(WEB, p), n: st.size });
  }
  return acc;
}
const files = walk(WEB)
  .filter((f) => !f.p.includes("node_modules") && !f.p.includes(".bak") && !f.p.includes("globe-raw"))
  .sort((a, b) => b.n - a.n)
  .slice(0, 25);
console.log("\nTop assets:");
for (const f of files) console.log((f.n / 1e6).toFixed(2) + "MB", f.p);
