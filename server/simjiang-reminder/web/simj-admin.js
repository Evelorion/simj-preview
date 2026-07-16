(() => {
  const $ = (id) => document.getElementById(id);
  const api = async (method, path, body) => {
    const res = await fetch(path, {
      method,
      credentials: 'include',
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined,
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok || data.ok === false) throw new Error(data.message || data.error || `HTTP ${res.status}`);
    return data;
  };

  const style = document.createElement('style');
  style.textContent = `
    .simj-admin{position:fixed;z-index:60;right:max(18px,env(safe-area-inset-right));top:82px;width:min(410px,calc(100vw - 36px));max-height:calc(100vh - 112px);overflow:auto;padding:14px;border-radius:24px;background:rgba(7,15,29,.78);border:1px solid rgba(255,255,255,.14);box-shadow:0 22px 70px rgba(0,0,0,.42);backdrop-filter:blur(18px) saturate(135%);color:#f5f9ff}
    .simj-admin h2{margin:0 0 4px;font-size:18px}.simj-admin p{margin:0;color:#9eb0ca;font-size:12px;line-height:1.55}.simj-admin-row{display:flex;gap:8px;align-items:center}.simj-admin-stack{display:grid;gap:10px}.simj-admin input{width:100%;border:1px solid rgba(255,255,255,.13);outline:0;border-radius:14px;background:rgba(255,255,255,.08);color:#fff;padding:12px;font:inherit}.simj-admin button{border:0;border-radius:14px;padding:11px 13px;background:linear-gradient(135deg,#5bd7ff,#806bff);color:#fff;font-weight:800;cursor:pointer;transition:transform .18s ease,opacity .18s ease,background .18s ease}.simj-admin button:active{transform:scale(.97)}.simj-admin button:disabled{cursor:not-allowed;opacity:.48}.simj-admin button.ghost{background:rgba(255,255,255,.08);color:#dbe7f5;border:1px solid rgba(255,255,255,.12)}.simj-admin button.warn{background:rgba(255,80,118,.16);border:1px solid rgba(255,80,118,.25);color:#ffd6df}.simj-tabs{display:grid;grid-template-columns:1fr 1fr;gap:8px;margin:12px 0}.simj-tabs button{background:rgba(255,255,255,.08)}.simj-tabs button.active{background:linear-gradient(135deg,rgba(91,215,255,.32),rgba(128,107,255,.28))}.simj-card{border:1px solid rgba(255,255,255,.1);background:rgba(255,255,255,.065);border-radius:18px;padding:12px}.simj-list{display:grid;gap:8px;margin-top:10px}.simj-country{display:flex;align-items:center;justify-content:space-between;gap:12px;border:1px solid rgba(255,255,255,.1);background:rgba(255,255,255,.07);border-radius:16px;padding:10px 12px;cursor:pointer;text-align:left;width:100%}.simj-country:hover{background:rgba(255,255,255,.11)}.simj-country strong{display:block;font-size:14px}.simj-country small{display:block;color:#9eb0ca;margin-top:3px}.simj-chip{min-width:46px;text-align:center;padding:7px 9px;border-radius:999px;background:rgba(91,215,255,.16);color:#bdefff;font-weight:900}.simj-message{min-height:18px;color:#73e0ff;font-size:12px}.simj-hidden{display:none!important}
    @media(max-width:900px){.simj-admin{top:auto;right:10px;left:10px;bottom:10px;width:auto;max-height:46vh}}
  `;
  document.head.appendChild(style);

  const root = document.createElement('section');
  root.className = 'simj-admin';
  root.innerHTML = `
    <div class="simj-admin-row" style="justify-content:space-between">
      <div><h2>SIMJ Web 地图</h2><p id="simj-subtitle">登录后查看你的 eSIM 覆盖</p></div>
      <button id="simj-logout" class="ghost simj-hidden">退出</button>
    </div>
    <div id="simj-message" class="simj-message"></div>
    <div id="simj-auth">
      <div class="simj-tabs"><button id="simj-tab-login" class="active">登录</button><button id="simj-tab-register">注册</button></div>
      <div class="simj-admin-stack">
        <input id="simj-user" autocomplete="username" placeholder="用户名 / 邮箱">
        <input id="simj-pass" autocomplete="current-password" placeholder="密码" type="password">
        <button id="simj-submit">登录</button>
      </div>
      <p id="simj-register-hint" style="margin-top:10px"></p>
    </div>
    <div id="simj-dashboard" class="simj-hidden">
      <div class="simj-card">
        <div class="simj-admin-row" style="justify-content:space-between">
          <div><strong id="simj-user-label">已登录</strong><p id="simj-counts">暂无数据</p></div>
          <span id="simj-role" class="simj-chip">USER</span>
        </div>
      </div>
      <div id="simj-admin-card" class="simj-card simj-hidden" style="margin-top:10px">
        <div class="simj-admin-row" style="justify-content:space-between">
          <div><strong>注册控制</strong><p id="simj-register-state">读取中</p></div>
          <button id="simj-toggle-register" class="ghost">切换</button>
        </div>
      </div>
      <div class="simj-card" style="margin-top:10px">
        <strong>覆盖国家/地区</strong>
        <p>地图只读取覆盖统计；具体号码保存在 App 端加密包里，服务器无法解密。</p>
        <div id="simj-country-list" class="simj-list"></div>
      </div>
    </div>
  `;
  document.body.appendChild(root);

  let mode = 'login';
  let coverage = { countries: [], countryCount: 0, records: 0, esims: 0 };
  let serverSettings = { allowRegistration: true, users: 0 };
  let canAdmin = false;

  const msg = (text) => { $('simj-message').textContent = text || ''; };
  function renderServerSettings() {
    const allowed = serverSettings.allowRegistration !== false;
    $('simj-tab-register').disabled = !allowed;
    $('simj-register-hint').textContent = allowed ? '服务器当前允许新用户注册。' : '服务器已暂停注册，请联系管理员。';
    const state = $('simj-register-state');
    const toggle = $('simj-toggle-register');
    if (state) state.textContent = allowed ? `允许注册 · ${serverSettings.users || 0} 个账户` : `已暂停注册 · ${serverSettings.users || 0} 个账户`;
    if (toggle) {
      toggle.textContent = allowed ? '暂停注册' : '允许注册';
      toggle.classList.toggle('warn', allowed);
      toggle.classList.toggle('ghost', !allowed);
    }
  }

  function setMode(next) {
    if (next === 'register' && serverSettings.allowRegistration === false) {
      mode = 'login';
      msg('服务器已暂停注册');
    } else {
      mode = next;
      msg('');
    }
    $('simj-tab-login').classList.toggle('active', mode === 'login');
    $('simj-tab-register').classList.toggle('active', mode === 'register');
    $('simj-submit').textContent = mode === 'login' ? '登录' : '注册账户';
    $('simj-pass').autocomplete = mode === 'login' ? 'current-password' : 'new-password';
  }

  async function loadPublicSettings() {
    try {
      const data = await api('GET', '/api/public-settings');
      serverSettings = { ...serverSettings, ...data };
      renderServerSettings();
      if (mode === 'register' && serverSettings.allowRegistration === false) setMode('login');
    } catch (_) {
      renderServerSettings();
    }
  }

  function findCountry(iso) {
    const code = String(iso || '').toUpperCase();
    const globe = window.SIMJ_GLOBE_API;
    return globe?.state?.countries?.find((c) => String(c.__iso2 || '').toUpperCase() === code) || null;
  }

  function focusCountry(iso) {
    const c = findCountry(iso);
    if (c && typeof window.SIMJ_GLOBE_API?.selectCountry === 'function') window.SIMJ_GLOBE_API.selectCountry(c, true);
  }

  function applyCoverage(nextCoverage) {
    coverage = nextCoverage || { countries: [] };
    const byIso = new Map((coverage.countries || []).map((c) => [String(c.iso || '').toUpperCase(), c]));
    const run = () => {
      const globe = window.SIMJ_GLOBE_API;
      const state = globe?.state;
      if (!state?.globe || !state.countries?.length) {
        setTimeout(run, 250);
        return;
      }
      const features = state.countries
        .filter((c) => byIso.has(String(c.__iso2 || '').toUpperCase()))
        .map((c) => ({ geometry: c.geometry, name: c.__nameZh, iso2: c.__iso2, __simjCoverage: byIso.get(String(c.__iso2 || '').toUpperCase()) }));
      state.globe.polygonsData(features);
    };
    run();
    renderCoverage();
  }

  function renderCoverage() {
    $('simj-counts').textContent = `${coverage.countryCount || 0} 个国家/地区 · ${coverage.esims || 0} 张 eSIM · ${coverage.records || 0} 张卡`;
    const list = $('simj-country-list');
    const items = coverage.countries || [];
    list.innerHTML = items.length ? '' : '<p>App 同步后，这里会显示覆盖国家/地区。</p>';
    items.forEach((item) => {
      const row = document.createElement('button');
      row.className = 'simj-country';
      row.innerHTML = `<span><strong>${item.name || item.iso}</strong><small>${item.esims || 0} 张 eSIM · ${item.records || 0} 张卡</small></span><span class="simj-chip">${item.iso || 'SIM'}</span>`;
      row.onclick = () => focusCountry(item.iso);
      list.appendChild(row);
    });
  }

  async function loadMe() {
    const me = await api('GET', '/api/account/me');
    $('simj-auth').classList.add('simj-hidden');
    $('simj-dashboard').classList.remove('simj-hidden');
    $('simj-logout').classList.remove('simj-hidden');
    $('simj-user-label').textContent = me.username || 'SIMJ 账户';
    canAdmin = !!me.canAdmin;
    $('simj-role').textContent = canAdmin ? 'ADMIN' : 'USER';
    $('simj-subtitle').textContent = canAdmin ? '管理员视图 · 可控制注册' : '用户视图 · 只能查看自己的地图';
    $('simj-admin-card').classList.toggle('simj-hidden', !canAdmin);
    serverSettings = { ...serverSettings, ...(me.serverSettings || {}) };
    renderServerSettings();
    applyCoverage(me.coverage || { countries: [], countryCount: 0, records: 0, esims: 0 });
  }

  $('simj-tab-login').onclick = () => setMode('login');
  $('simj-tab-register').onclick = () => setMode('register');
  $('simj-submit').onclick = async () => {
    try {
      const username = $('simj-user').value.trim();
      const password = $('simj-pass').value;
      if (!username || password.length < 8) throw new Error('请输入账号和至少 8 位密码');
      if (mode === 'login') {
        await api('POST', '/api/account/login', { username, password });
        await loadMe();
        msg('登录成功');
      } else {
        if (serverSettings.allowRegistration === false) throw new Error('服务器已暂停注册');
        await api('POST', '/api/account/register', { username, password });
        await loadMe();
        msg('注册成功，请在 App 里登录同一账号同步号码');
      }
    } catch (e) {
      msg(e.message);
    }
  };

  $('simj-toggle-register').onclick = async () => {
    if (!canAdmin) return msg('普通账户不能管理后端');
    try {
      const next = serverSettings.allowRegistration === false;
      const data = await api('POST', '/api/admin/settings', { allowRegistration: next });
      serverSettings = { ...serverSettings, ...data };
      renderServerSettings();
      if (mode === 'register' && serverSettings.allowRegistration === false) setMode('login');
      msg(next ? '已允许新用户注册' : '已暂停新用户注册');
    } catch (e) {
      msg(e.message);
    }
  };

  $('simj-logout').onclick = async () => {
    await api('POST', '/api/account/logout', {});
    location.reload();
  };

  renderServerSettings();
  loadPublicSettings();
  setMode('login');
  loadMe().catch(() => {});
  setInterval(() => {
    const state = window.SIMJ_GLOBE_API?.state;
    if ((coverage.countries || []).length && state?.globe && !(state.globe.polygonsData?.() || []).length) applyCoverage(coverage);
  }, 1800);
})();
