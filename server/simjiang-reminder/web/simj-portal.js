/**
 * simJ Web Portal — login + map highlight + full number cards (account session).
 * Numbers come from App coverage samples (auth-only) and/or E2EE vault decrypt.
 */
(() => {
  const $ = (id) => document.getElementById(id);
  const SIMJ_AAD = new TextEncoder().encode("simj:e2ee:v1");
  const SIMJ_ITER = 310000;

  async function api(method, path, body) {
    const res = await fetch(path, {
      method,
      credentials: "include",
      headers: body ? { "Content-Type": "application/json" } : {},
      body: body ? JSON.stringify(body) : undefined,
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok || data.ok === false) {
      throw new Error(data.message || data.error || `HTTP ${res.status}`);
    }
    return data;
  }

  /* ---------- crypto (match App password-derived vault) ---------- */
  function b64uToBytes(text) {
    const s = String(text || "").replace(/-/g, "+").replace(/_/g, "/");
    const pad = "=".repeat((4 - (s.length % 4)) % 4);
    const bin = atob(s + pad);
    const out = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
    return out;
  }
  function bytesToB64u(bytes) {
    let bin = "";
    const arr = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
    for (let i = 0; i < arr.length; i++) bin += String.fromCharCode(arr[i]);
    return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
  }
  async function deriveSimjCloudSecret(username, password) {
    const saltSrc = new TextEncoder().encode("simj:e2ee:v1:" + String(username || "").trim().toLowerCase());
    const hash = await crypto.subtle.digest("SHA-256", saltSrc);
    const salt = new Uint8Array(hash).slice(0, 16);
    const material = await crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(password),
      "PBKDF2",
      false,
      ["deriveBits"]
    );
    const bits = await crypto.subtle.deriveBits(
      { name: "PBKDF2", salt, iterations: SIMJ_ITER, hash: "SHA-256" },
      material,
      256
    );
    return bytesToB64u(new Uint8Array(bits));
  }
  async function decryptVaultEnvelope(envelope, secretB64u) {
    const keyBytes = b64uToBytes(secretB64u);
    const key = await crypto.subtle.importKey("raw", keyBytes, "AES-GCM", false, ["decrypt"]);
    const nonce = b64uToBytes(envelope.nonce);
    const ct = b64uToBytes(envelope.ciphertext || envelope.cipherText || "");
    const tag = b64uToBytes(envelope.tag || "");
    const sealed = new Uint8Array(ct.length + tag.length);
    sealed.set(ct, 0);
    if (tag.length) sealed.set(tag, ct.length);
    const plain = await crypto.subtle.decrypt(
      { name: "AES-GCM", iv: nonce, additionalData: SIMJ_AAD, tagLength: 128 },
      key,
      sealed
    );
    return JSON.parse(new TextDecoder().decode(plain));
  }

  /* ---------- styles ---------- */
  const style = document.createElement("style");
  style.textContent = `
    .simj-portal{
      position:fixed;z-index:90;
      right:max(16px,env(safe-area-inset-right));
      top:max(16px,env(safe-area-inset-top));
      left:auto;bottom:auto;
      width:min(340px,calc(100vw - 28px));
      max-height:calc(100vh - 28px);
      overflow:auto;padding:14px;
      border-radius:20px;
      background:rgba(7,15,29,.88);
      border:1px solid rgba(255,255,255,.14);
      box-shadow:0 18px 60px rgba(0,0,0,.5);
      backdrop-filter:blur(18px) saturate(135%);
      color:#f5f9ff;
      font-family:Inter,system-ui,"Microsoft YaHei",sans-serif;
    }
    .simj-portal h2{margin:0;font-size:16px;font-weight:800}
    .simj-portal p{margin:0;color:#9eb0ca;font-size:12px;line-height:1.5}
    .simj-stack{display:grid;gap:8px;margin-top:10px}
    .simj-row{display:flex;gap:8px;align-items:center}
    .simj-portal input,.simj-portal select{
      width:100%;border:1px solid rgba(255,255,255,.13);outline:0;border-radius:12px;
      background:rgba(255,255,255,.08);color:#fff;padding:11px;font:inherit
    }
    .simj-portal button{
      border:0;border-radius:12px;padding:10px 12px;
      background:linear-gradient(135deg,#5bd7ff,#806bff);color:#fff;
      font-weight:800;cursor:pointer;font:inherit
    }
    .simj-portal button:disabled{opacity:.45;cursor:not-allowed}
    .simj-portal button.ghost{
      background:rgba(255,255,255,.08);color:#dbe7f5;border:1px solid rgba(255,255,255,.12)
    }
    .simj-portal button.block{width:100%}
    .simj-tabs{display:grid;grid-template-columns:1fr 1fr 1fr;gap:6px}
    .simj-tabs button{background:rgba(255,255,255,.08);padding:9px 6px;font-size:12px}
    .simj-tabs button.active{background:linear-gradient(135deg,rgba(91,215,255,.35),rgba(128,107,255,.3))}
    .simj-card{
      border:1px solid rgba(255,255,255,.1);background:rgba(255,255,255,.05);
      border-radius:14px;padding:10px 12px;margin-top:8px
    }
    .simj-msg{min-height:16px;color:#73e0ff;font-size:12px;margin-top:6px;white-space:pre-wrap}
    .simj-msg.err{color:#ff8eaa}
    .simj-hidden{display:none!important}
    .simj-chip{
      display:inline-flex;align-items:center;padding:4px 9px;border-radius:999px;
      background:rgba(91,215,255,.16);color:#bdefff;font-weight:800;font-size:11px
    }
    .simj-stats{
      display:grid;grid-template-columns:1fr 1fr 1fr;gap:6px;margin-top:8px
    }
    .simj-stat{
      background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.08);
      border-radius:12px;padding:8px 6px;text-align:center
    }
    .simj-stat b{display:block;font-size:18px;font-weight:800;color:#fff;line-height:1.15}
    .simj-stat span{display:block;font-size:10px;color:#8fa3bd;margin-top:2px}
    .simj-fold{margin-top:8px;border:1px solid rgba(255,255,255,.1);border-radius:14px;overflow:hidden}
    .simj-fold>summary{
      list-style:none;cursor:pointer;user-select:none;
      padding:10px 12px;background:rgba(255,255,255,.05);
      display:flex;align-items:center;justify-content:space-between;gap:8px;
      font-size:13px;font-weight:700
    }
    .simj-fold>summary::-webkit-details-marker{display:none}
    .simj-fold>summary::after{content:"▾";opacity:.7;font-size:12px}
    .simj-fold[open]>summary::after{content:"▴"}
    .simj-fold-body{padding:10px 12px 12px}
    .simj-open-all{
      margin-top:10px;width:100%;padding:14px 14px;
      border-radius:16px;border:1px solid rgba(91,215,255,.35);
      background:linear-gradient(135deg,rgba(91,215,255,.22),rgba(128,107,255,.28));
      color:#fff;font-weight:800;cursor:pointer;font:inherit;text-align:left;
      display:flex;align-items:center;justify-content:space-between;gap:10px
    }
    .simj-open-all:hover{filter:brightness(1.08)}
    .simj-open-all strong{display:block;font-size:15px}
    .simj-open-all small{display:block;margin-top:3px;font-size:11px;opacity:.85;font-weight:600}
    .simj-keybox{
      word-break:break-all;font-family:ui-monospace,Consolas,monospace;font-size:12px;
      padding:10px;border-radius:12px;background:rgba(0,0,0,.35);
      border:1px dashed rgba(255,120,150,.45);color:#ffe3ea;line-height:1.45
    }
    .simj-admin-link{display:none!important}

    /* full-screen card gallery */
    .simj-gallery{
      position:fixed;inset:0;z-index:200;
      background:rgba(3,6,14,.78);
      backdrop-filter:blur(10px);
      display:flex;align-items:center;justify-content:center;
      padding:max(16px,env(safe-area-inset-top)) 16px 16px;
    }
    .simj-gallery-panel{
      width:min(920px,100%);
      max-height:min(88vh,900px);
      overflow:hidden;
      border-radius:22px;
      border:1px solid rgba(255,255,255,.14);
      background:rgba(10,16,30,.95);
      box-shadow:0 24px 80px rgba(0,0,0,.55);
      color:#f5f9ff;
      display:flex;flex-direction:column;
      font-family:Inter,system-ui,"Microsoft YaHei",sans-serif;
    }
    .simj-gallery-hd{
      display:flex;align-items:center;justify-content:space-between;gap:12px;
      padding:16px 18px;border-bottom:1px solid rgba(255,255,255,.08)
    }
    .simj-gallery-hd h3{margin:0;font-size:17px;font-weight:800}
    .simj-gallery-hd p{margin:3px 0 0;font-size:12px;color:#9eb0ca}
    .simj-gallery-tools{display:flex;gap:8px;flex-wrap:wrap;align-items:center}
    .simj-gallery-tools button{
      border:0;border-radius:12px;padding:9px 12px;cursor:pointer;font:inherit;font-weight:700;
      background:rgba(255,255,255,.08);color:#e8f1ff;border:1px solid rgba(255,255,255,.1)
    }
    .simj-gallery-tools button.active{
      background:linear-gradient(135deg,rgba(91,215,255,.35),rgba(128,107,255,.35));
      border-color:transparent;color:#fff
    }
    .simj-gallery-body{
      padding:14px;overflow:auto;flex:1;
      display:grid;
      grid-template-columns:repeat(auto-fill,minmax(220px,1fr));
      gap:12px;align-content:start
    }
    .simj-phone-card{
      border-radius:16px;padding:14px;
      background:linear-gradient(160deg,rgba(255,255,255,.08),rgba(255,255,255,.03));
      border:1px solid rgba(255,255,255,.12);
      min-height:118px;display:flex;flex-direction:column;gap:8px;
      cursor:pointer;transition:transform .15s ease,border-color .15s
    }
    .simj-phone-card:hover{transform:translateY(-2px);border-color:rgba(91,215,255,.45)}
    .simj-phone-card .top{display:flex;justify-content:space-between;align-items:center;gap:8px}
    .simj-phone-card .op{font-size:14px;font-weight:800;color:#fff}
    .simj-phone-card .num{
      font-family:ui-monospace,Consolas,monospace;
      font-size:16px;font-weight:700;letter-spacing:.3px;color:#e8f7ff;word-break:break-all
    }
    .simj-phone-card .meta{font-size:11px;color:#93a6bf;line-height:1.45}
    .simj-phone-card .tag{
      font-size:10px;padding:3px 8px;border-radius:999px;font-weight:800;
      background:rgba(56,189,248,.2);color:#bae6fd
    }
    .simj-phone-card .tag.sim{background:rgba(255,255,255,.1);color:#c9d5e6}
    .simj-empty{grid-column:1/-1;text-align:center;color:#9eb0ca;padding:40px 12px;font-size:13px;line-height:1.6}

    @media(max-width:900px){
      .simj-portal{
        top:auto!important;bottom:10px;right:10px;left:10px;
        width:auto;max-height:46vh
      }
      .simj-gallery-body{grid-template-columns:1fr}
    }
  `;
  document.head.appendChild(style);

  const hideControls = () => {
    const el = document.getElementById("controls");
    if (el) el.style.display = "none";
  };
  hideControls();
  setTimeout(hideControls, 200);

  const root = document.createElement("section");
  root.className = "simj-portal";
  root.innerHTML = `
    <div class="simj-row" style="justify-content:space-between;align-items:flex-start">
      <div>
        <h2>SIMJ 云端地图</h2>
        <p id="simj-sub">登录 · 统计 · 高亮</p>
      </div>
      <div class="simj-row">
        <button id="simj-rotate" class="ghost" type="button" title="地球自转">⏸ 自转</button>
        <button id="simj-logout" class="ghost simj-hidden" type="button">退出</button>
      </div>
    </div>
    <div id="simj-msg" class="simj-msg"></div>

    <div id="simj-auth">
      <div class="simj-tabs" style="margin-top:10px">
        <button type="button" id="tab-login" class="active">登录</button>
        <button type="button" id="tab-register">注册</button>
        <button type="button" id="tab-reset">重置</button>
      </div>
      <div class="simj-stack">
        <input id="simj-user" autocomplete="username" placeholder="用户名">
        <input id="simj-pass" autocomplete="current-password" type="password" placeholder="密码（至少 8 位）">
        <input id="simj-pass2" class="simj-hidden" type="password" placeholder="确认密码 / 新密码">
        <input id="simj-pkey" class="simj-hidden" type="password" placeholder="私钥（重置时必填）" autocomplete="off">
        <button type="button" id="simj-submit" class="block">登录</button>
      </div>
      <p id="simj-reg-hint" style="margin-top:8px"></p>
    </div>

    <div id="simj-key-once" class="simj-hidden" style="margin-top:10px">
      <div class="simj-card">
        <strong style="color:#ff8eaa">⚠️ 请立即保存找回密码私钥</strong>
        <p style="margin-top:6px">私钥仅用于忘记密码时重置。日常登录与云端恢复只需账号+密码；服务器不保存此私钥。</p>
        <div id="simj-key-value" class="simj-keybox" style="margin-top:8px"></div>
        <div class="simj-row" style="margin-top:8px">
          <button type="button" id="simj-copy-key">复制私钥</button>
          <button type="button" id="simj-key-done" class="ghost">我已保存</button>
        </div>
      </div>
    </div>

    <div id="simj-dash" class="simj-hidden">
      <div class="simj-card">
        <div class="simj-row" style="justify-content:space-between">
          <div>
            <strong id="simj-who">已登录</strong>
            <p id="simj-hintline" style="margin-top:2px">App 同步后显示完整号码卡片</p>
          </div>
          <span id="simj-role" class="simj-chip">USER</span>
        </div>
        <div class="simj-stats">
          <div class="simj-stat"><b id="stat-countries">0</b><span>号码国家</span></div>
          <div class="simj-stat"><b id="stat-cards">0</b><span>号码总数</span></div>
          <div class="simj-stat"><b id="stat-esim">0</b><span>eSIM</span></div>
        </div>
      </div>

      <button type="button" class="simj-open-all" id="simj-open-numbers">
        <span>
          <strong>我的号码</strong>
          <small id="simj-open-sub">点击查看全部 eSIM / SIM 卡片</small>
        </span>
        <span class="simj-chip" id="simj-open-count">0</span>
      </button>

      <details class="simj-fold" id="fold-help">
        <summary><span>说明 / 管理</span></summary>
        <div class="simj-fold-body">
          <p>1. App 登录同一账号 →「同步到云端」</p>
          <p>2. 地球高亮 = App 有号码的国家（eSIM 更深）</p>
          <p>3. 完整号码仅你的账号可见；点国家「App 号码」或上方按钮看卡片</p>
          <a href="/admin" class="simj-chip" style="margin-top:10px;text-decoration:none">管理员入口 →</a>
        </div>
      </details>
    </div>
  `;
  document.body.appendChild(root);

  let mode = "login";
  let coverage = { countries: [], countryCount: 0, records: 0, esims: 0 };
  let serverSettings = { allowRegistration: true, users: 0 };
  let pendingPrivateKey = "";
  let rotating = true;
  /** @type {Array<any>} decrypted / coverage-derived cards */
  let allCards = [];
  let galleryFilter = "all"; // all | esim | sim
  let galleryIso = ""; // optional country filter
  let currentUsername = "";

  function msg(text, err = false) {
    const el = $("simj-msg");
    el.textContent = text || "";
    el.classList.toggle("err", !!err);
  }

  function setMode(next) {
    if (next === "register" && serverSettings.allowRegistration === false) {
      mode = "login";
      msg("服务器已暂停注册", true);
    } else {
      mode = next;
      msg("");
    }
    $("tab-login").classList.toggle("active", mode === "login");
    $("tab-register").classList.toggle("active", mode === "register");
    $("tab-reset").classList.toggle("active", mode === "reset");
    $("simj-pass2").classList.toggle("simj-hidden", mode === "login");
    $("simj-pkey").classList.toggle("simj-hidden", mode !== "reset");
    $("simj-pass").placeholder =
      mode === "reset" ? "新密码（至少 8 位）" : mode === "register" ? "设置密码（至少 8 位）" : "密码（至少 8 位）";
    $("simj-pass2").placeholder = mode === "reset" ? "确认新密码" : "确认密码";
    $("simj-submit").textContent =
      mode === "login" ? "登录" : mode === "register" ? "注册账户" : "用私钥重置密码";
  }

  function renderSettings() {
    const allowed = serverSettings.allowRegistration !== false;
    $("tab-register").disabled = !allowed;
    $("simj-reg-hint").textContent = allowed
      ? "注册后私钥只显示一次（仅用于找回密码）。App 与网页共用账号，日常登录即可自动同步。"
      : "服务器已暂停注册，请联系管理员。";
  }

  async function loadPublic() {
    try {
      const data = await api("GET", "/api/public-settings");
      serverSettings = { ...serverSettings, ...data };
      renderSettings();
      if (mode === "register" && !serverSettings.allowRegistration) setMode("login");
    } catch (_) {
      renderSettings();
    }
  }

  function waitGlobe(cb, tries = 80) {
    if (window.SIMJ_GLOBE_STATE?.globe) return cb(window.SIMJ_GLOBE_STATE);
    if (tries <= 0) return;
    setTimeout(() => waitGlobe(cb, tries - 1), 250);
  }

  function isEsimLike(r) {
    if (r.esim === true || r.isEsim === true) return true;
    const text = [r.cardType, r.eid, r.smdp, r.activationCode, r.note, r.tags]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();
    return text.includes("esim") || !!(r.eid || r.smdp || r.activationCode);
  }

  function cardKey(c) {
    return c.id || `${c.iso || ""}:${c.code || ""}:${c.number || c.last4 || ""}:${c.op || ""}`;
  }

  function normalizeCard(raw, isoFallback = "", nameFallback = "") {
    const number = String(raw.number || raw.num || "").trim();
    const last4 =
      String(raw.last4 || "").replace(/\D/g, "").slice(-4) ||
      number.replace(/\D/g, "").slice(-4) ||
      "????";
    const display = number || raw.mask || `•••• ${last4}`;
    const code = String(raw.code || raw.countryCode || "").trim();
    const esim = isEsimLike(raw);
    return {
      id: String(raw.id || ""),
      iso: String(raw.iso || isoFallback || "").toUpperCase(),
      name: String(raw.name || raw.countryName || nameFallback || ""),
      flag: String(raw.flag || ""),
      number,
      last4,
      display,
      code,
      op: String(raw.op || raw.operator || raw.name || "号码").trim() || "号码",
      esim,
      expire: String(raw.expire || raw.expireDate || ""),
      balance: String(raw.balance || ""),
      note: String(raw.note || ""),
      signal: String(raw.signal || raw.signalStatus || ""),
      cardType: String(raw.cardType || ""),
    };
  }

  function cardsFromCoverage(cov) {
    const out = [];
    const seen = new Set();
    (cov.countries || []).forEach((country) => {
      const iso = String(country.iso || "").toUpperCase();
      const cname = country.name || iso;
      (country.samples || []).forEach((s) => {
        const card = normalizeCard(s, iso, cname);
        const k = cardKey(card);
        if (seen.has(k)) return;
        seen.add(k);
        out.push(card);
      });
    });
    return out;
  }

  function cardsFromVaultRecords(records) {
    if (!Array.isArray(records)) return [];
    return records.map((r) =>
      normalizeCard(
        {
          id: r.id,
          number: r.number,
          code: r.countryCode,
          op: r.operator,
          name: r.countryName,
          flag: r.flag,
          expire: r.expireDate,
          balance: r.balance,
          note: r.note,
          signal: r.signalStatus,
          cardType: r.cardType,
          eid: r.eid,
          smdp: r.smdp,
          activationCode: r.activationCode,
          tags: r.tags,
          esim: isEsimLike(r),
        },
        "",
        r.countryName || ""
      )
    );
  }

  function mergeCards(primary, secondary) {
    const map = new Map();
    [...secondary, ...primary].forEach((c) => {
      const k = cardKey(c);
      const prev = map.get(k);
      if (!prev) map.set(k, c);
      else {
        // prefer full number / richer fields
        map.set(k, {
          ...prev,
          ...c,
          number: c.number || prev.number,
          display: c.number || prev.number || c.display || prev.display,
          op: c.op && c.op !== "号码" ? c.op : prev.op,
        });
      }
    });
    return [...map.values()];
  }

  function rebuildCards() {
    const fromCov = cardsFromCoverage(coverage);
    allCards = mergeCards(window.__SIMJ_VAULT_CARDS || [], fromCov);
    window.__SIMJ_ALL_CARDS = allCards;
    const n = allCards.length;
    const esimN = allCards.filter((c) => c.esim).length;
    if ($("simj-open-count")) $("simj-open-count").textContent = String(n || coverage.records || 0);
    if ($("simj-open-sub")) {
      $("simj-open-sub").textContent = n
        ? `${n} 张卡 · ${esimN} eSIM · 点开卡片排列`
        : "App 同步后点此查看全部号码卡片";
    }
  }

  function applyCoverage(next) {
    coverage = next || { countries: [] };
    window.__SIMJ_LAST_COVERAGE = coverage;

    const allItems = (coverage.countries || []).filter(
      (c) => Number(c.records || 0) > 0 || Number(c.esims || 0) > 0
    );
    const esimItems = allItems.filter((c) => Number(c.esims || 0) > 0);
    const appIsos = new Set(allItems.map((c) => String(c.iso || "").toUpperCase()).filter(Boolean));
    const esimIsos = new Set(esimItems.map((c) => String(c.iso || "").toUpperCase()).filter(Boolean));

    const pushHighlight = () => {
      if (typeof window.SIMJ_SET_COVERAGE_HIGHLIGHT === "function") {
        window.SIMJ_SET_COVERAGE_HIGHLIGHT({ appIsos, esimIsos, items: allItems });
        return true;
      }
      if (typeof window.SIMJ_SET_ESIM_HIGHLIGHT === "function") {
        window.SIMJ_SET_ESIM_HIGHLIGHT(appIsos, allItems);
        return true;
      }
      return false;
    };

    if (!pushHighlight()) {
      waitGlobe(() => pushHighlight());
      window.addEventListener("simj-globe-ready", () => pushHighlight(), { once: true });
    }

    const countryN = allItems.length || Number(coverage.countryCount || 0);
    const cardsN = Number(coverage.records || 0) || allItems.reduce((s, c) => s + Number(c.records || 0), 0);
    const esimN = Number(coverage.esims || 0) || esimItems.reduce((s, c) => s + Number(c.esims || 0), 0);
    if ($("stat-countries")) $("stat-countries").textContent = String(countryN);
    if ($("stat-cards")) $("stat-cards").textContent = String(cardsN);
    if ($("stat-esim")) $("stat-esim").textContent = String(esimN);

    rebuildCards();
  }

  function getCards(filterIso = "", filterType = "all") {
    let list = allCards.slice();
    if (filterIso) {
      const iso = filterIso.toUpperCase();
      list = list.filter((c) => c.iso === iso || (!c.iso && String(c.name || "").includes(iso)));
      // also match by coverage country name samples without iso from vault
      if (!list.length) {
        const country = (coverage.countries || []).find((x) => String(x.iso || "").toUpperCase() === iso);
        if (country) {
          list = cardsFromCoverage({ countries: [country] });
        }
      }
    }
    if (filterType === "esim") list = list.filter((c) => c.esim);
    if (filterType === "sim") list = list.filter((c) => !c.esim);
    list.sort((a, b) => {
      if (a.esim !== b.esim) return a.esim ? -1 : 1;
      return String(a.op).localeCompare(String(b.op)) || String(a.display).localeCompare(String(b.display));
    });
    return list;
  }

  function closeGallery() {
    const el = document.getElementById("simj-gallery");
    if (el) el.remove();
  }

  function openGallery(opts = {}) {
    galleryIso = String(opts.iso || "").toUpperCase();
    galleryFilter = opts.filter || "all";
    closeGallery();

    const list = getCards(galleryIso, galleryFilter);
    const wrap = document.createElement("div");
    wrap.id = "simj-gallery";
    wrap.className = "simj-gallery";
    wrap.innerHTML = `
      <div class="simj-gallery-panel" role="dialog" aria-modal="true">
        <div class="simj-gallery-hd">
          <div>
            <h3 id="simj-g-title">${galleryIso ? `我的号码 · ${galleryIso}` : "我的号码"}</h3>
            <p id="simj-g-sub">完整号码仅本账号可见 · 与 App 同步一致</p>
          </div>
          <div class="simj-gallery-tools">
            <button type="button" data-f="all" class="${galleryFilter === "all" ? "active" : ""}">全部</button>
            <button type="button" data-f="esim" class="${galleryFilter === "esim" ? "active" : ""}">eSIM</button>
            <button type="button" data-f="sim" class="${galleryFilter === "sim" ? "active" : ""}">SIM</button>
            <button type="button" id="simj-g-close">关闭</button>
          </div>
        </div>
        <div class="simj-gallery-body" id="simj-g-body"></div>
      </div>
    `;
    document.body.appendChild(wrap);

    const body = wrap.querySelector("#simj-g-body");
    const render = () => {
      const cards = getCards(galleryIso, galleryFilter);
      const sub = wrap.querySelector("#simj-g-sub");
      if (sub) sub.textContent = `${cards.length} 张卡片 · 点卡片可复制号码`;
      body.innerHTML = "";
      if (!cards.length) {
        body.innerHTML = `<div class="simj-empty">暂无号码卡片。<br>请在最新版 App 登录同一账号后点「同步到云端」。</div>`;
        return;
      }
      cards.forEach((c) => {
        const el = document.createElement("div");
        el.className = "simj-phone-card";
        const title = c.op || c.name || "号码";
        const fullNum = [c.code, c.display].filter(Boolean).join(" ").trim();
        const metaBits = [
          c.name || c.iso,
          c.expire ? `到期 ${c.expire}` : "",
          c.balance ? `余额 ${c.balance}` : "",
          c.signal || "",
        ].filter(Boolean);
        el.innerHTML = `
          <div class="top">
            <span class="op">${c.flag ? c.flag + " " : ""}${escapeHtml(title)}</span>
            <span class="tag ${c.esim ? "" : "sim"}">${c.esim ? "eSIM" : "SIM"}</span>
          </div>
          <div class="num">${escapeHtml(fullNum || "—")}</div>
          <div class="meta">${escapeHtml(metaBits.join(" · ") || "App 已同步")}</div>
        `;
        el.onclick = async () => {
          try {
            await navigator.clipboard.writeText(fullNum || c.display);
            msg("已复制：" + (fullNum || c.display));
          } catch (_) {
            msg(fullNum || c.display);
          }
        };
        body.appendChild(el);
      });
    };

    wrap.querySelectorAll("[data-f]").forEach((btn) => {
      btn.addEventListener("click", () => {
        galleryFilter = btn.getAttribute("data-f") || "all";
        wrap.querySelectorAll("[data-f]").forEach((b) => b.classList.toggle("active", b === btn));
        render();
      });
    });
    wrap.querySelector("#simj-g-close").onclick = closeGallery;
    wrap.addEventListener("click", (e) => {
      if (e.target === wrap) closeGallery();
    });
    render();
  }

  function escapeHtml(s) {
    return String(s || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  async function pullVaultIfPossible(username, password) {
    try {
      let secret = sessionStorage.getItem("simj_vault_secret") || "";
      const user = username || sessionStorage.getItem("simj_vault_user") || currentUsername || "";
      if (password && user) {
        secret = await deriveSimjCloudSecret(user, password);
        sessionStorage.setItem("simj_vault_secret", secret);
        sessionStorage.setItem("simj_vault_user", user);
      }
      if (!secret) return false;
      const sync = await api("GET", "/api/sync");
      const env = sync.encryptedVault || sync.envelope;
      if (!env || !env.ciphertext) return false;
      const payload = await decryptVaultEnvelope(env, secret);
      const records = Array.isArray(payload.records) ? payload.records : [];
      window.__SIMJ_VAULT_CARDS = cardsFromVaultRecords(records);
      rebuildCards();
      return records.length > 0;
    } catch (e) {
      console.warn("[SIMJ] vault decrypt", e);
      return false;
    }
  }

  async function loadMe(opts = {}) {
    const me = await api("GET", "/api/account/me");
    $("simj-auth").classList.add("simj-hidden");
    $("simj-dash").classList.remove("simj-hidden");
    $("simj-logout").classList.remove("simj-hidden");
    currentUsername = me.username || "";
    $("simj-who").textContent = currentUsername || "SIMJ";
    $("simj-role").textContent = me.canAdmin ? String(me.role || "ADMIN").toUpperCase() : "USER";
    $("simj-sub").textContent = "号码卡片 · 地图高亮";
    if ($("simj-hintline")) {
      $("simj-hintline").textContent = "同账号可见完整号码 · 点下方按钮看卡片";
    }
    serverSettings = { ...serverSettings, ...(me.serverSettings || {}) };
    renderSettings();
    applyCoverage(me.coverage || { countries: [] });

    if (opts.password) {
      const ok = await pullVaultIfPossible(currentUsername, opts.password);
      if (ok) msg("登录成功 · 已解密云端完整号码");
      else msg("登录成功 · 已加载同步号码（如缺完整号请在 App 再同步一次）");
    } else {
      await pullVaultIfPossible();
    }
  }

  function showPrivateKeyOnce(key) {
    pendingPrivateKey = key || "";
    $("simj-key-value").textContent = pendingPrivateKey;
    $("simj-key-once").classList.remove("simj-hidden");
  }

  function syncRotateBtn() {
    const btn = $("simj-rotate");
    if (!btn) return;
    btn.textContent = rotating ? "⏸ 自转" : "▶ 自转";
  }
  function setRotate(v) {
    rotating = !!v;
    try {
      const globe = window.SIMJ_GLOBE_STATE?.globe;
      const c = globe?.controls?.();
      if (c) c.autoRotate = rotating;
      const old = document.getElementById("toggleBtn");
      if (old) old.innerHTML = rotating ? "⏸️ 暂停自转" : "▶️ 开始自转";
    } catch (_) {}
    syncRotateBtn();
  }
  $("simj-rotate").onclick = () => setRotate(!rotating);

  window.addEventListener(
    "simj-globe-ready",
    () => {
      try {
        const globe = window.SIMJ_GLOBE_STATE?.globe;
        const c = globe?.controls?.();
        if (c) rotating = !!c.autoRotate;
      } catch (_) {}
      syncRotateBtn();
      hideControls();
    },
    { once: false }
  );

  const bindOldRotate = () => {
    const old = document.getElementById("toggleBtn");
    if (!old || old.__simjBound) return;
    old.__simjBound = true;
    old.addEventListener("click", () => {
      setTimeout(() => {
        try {
          const globe = window.SIMJ_GLOBE_STATE?.globe;
          rotating = !!(globe?.controls?.()?.autoRotate);
          syncRotateBtn();
        } catch (_) {}
      }, 50);
    });
  };
  setInterval(bindOldRotate, 1000);

  $("tab-login").onclick = () => setMode("login");
  $("tab-register").onclick = () => setMode("register");
  $("tab-reset").onclick = () => setMode("reset");

  $("simj-submit").onclick = async () => {
    try {
      const username = $("simj-user").value.trim();
      const password = $("simj-pass").value;
      const password2 = $("simj-pass2").value;
      const pkey = $("simj-pkey").value.trim();
      if (!username) throw new Error("请输入用户名");

      if (mode === "login") {
        if (password.length < 8) throw new Error("请输入至少 8 位密码");
        await api("POST", "/api/account/login", { username, password });
        await loadMe({ password });
        $("simj-pass").value = "";
        return;
      }
      if (mode === "register") {
        if (serverSettings.allowRegistration === false) throw new Error("服务器已暂停注册");
        if (password.length < 8) throw new Error("密码至少 8 位");
        if (password !== password2) throw new Error("两次密码不一致");
        const data = await api("POST", "/api/account/register", { username, password });
        if (data.privateKey) showPrivateKeyOnce(data.privateKey);
        await loadMe({ password });
        $("simj-pass").value = "";
        $("simj-pass2").value = "";
        msg("注册成功 — 私钥仅用于找回密码，请另存；日常登录只需账号密码");
        return;
      }
      if (!pkey) throw new Error("请填写私钥");
      if (password.length < 8) throw new Error("新密码至少 8 位");
      if (password !== password2) throw new Error("两次新密码不一致");
      const data = await api("POST", "/api/account/reset-password", {
        username,
        privateKey: pkey,
        newPassword: password,
      });
      msg(data.message || "密码已重置，请用新密码登录");
      setMode("login");
      $("simj-pass").value = "";
      $("simj-pass2").value = "";
      $("simj-pkey").value = "";
    } catch (e) {
      msg(e.message || String(e), true);
    }
  };

  $("simj-copy-key").onclick = async () => {
    try {
      await navigator.clipboard.writeText(pendingPrivateKey);
      msg("私钥已复制");
    } catch (_) {
      msg("复制失败，请手动全选", true);
    }
  };
  $("simj-key-done").onclick = () => $("simj-key-once").classList.add("simj-hidden");
  $("simj-logout").onclick = async () => {
    try {
      await api("POST", "/api/account/logout", {});
    } catch (_) {}
    sessionStorage.removeItem("simj_vault_secret");
    sessionStorage.removeItem("simj_vault_user");
    location.reload();
  };

  $("simj-open-numbers").onclick = () => openGallery({ filter: "all" });

  window.addEventListener("simj-globe-ready", (ev) => {
    const detail = ev.detail || {};
    window.SIMJ_GLOBE_STATE = {
      globe: detail.globe,
      countries: detail.countries || [],
    };
    if (typeof detail.selectCountry === "function") {
      window.SIMJ_SELECT_COUNTRY = detail.selectCountry;
    }
    if ((coverage.countries || []).length) applyCoverage(coverage);
  });

  window.SIMJ_PORTAL = {
    applyCoverage,
    openGallery,
    getCards,
    getRecordsByIso(iso) {
      return getCards(String(iso || "").toUpperCase(), "all");
    },
    setGlobeState(state) {
      window.SIMJ_GLOBE_STATE = state;
      if ((coverage.countries || []).length) applyCoverage(coverage);
    },
  };

  // country panel: click App 号码 → open cards for that country
  document.addEventListener(
    "click",
    (e) => {
      const card = e.target.closest?.("#esimCard");
      if (!card) return;
      e.preventDefault();
      e.stopPropagation();
      const iso =
        document.getElementById("isoCircleIcon")?.textContent?.trim()?.toUpperCase() ||
        (typeof window.SIMJ_GET_SELECTED_ISO === "function" ? window.SIMJ_GET_SELECTED_ISO() : "") ||
        "";
      openGallery({ iso: iso.length === 2 ? iso : "", filter: "all" });
    },
    true
  );

  renderSettings();
  loadPublic();
  setMode("login");
  syncRotateBtn();
  loadMe().catch(() => {});
  setInterval(() => {
    if ($("simj-dash").classList.contains("simj-hidden")) return;
    api("GET", "/api/account/coverage")
      .then((d) => applyCoverage(d.coverage || {}))
      .catch(() => {});
  }, 30000);
})();
