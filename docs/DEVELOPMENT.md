# SIMJ 全栈开发文档（接手必读）

> 目标：不翻聊天记录也能改 **Android App / 3D 地球 Web / Python 云端 / 管理后台**。
> 最后对齐：后端 `v7-plain-sync` · App `3.0.24-pre` · Web 缓存 `?v=plain-sync-map-tibet-merge-20260726`
> 仓库根目录：`simj-preview/`

---

## 0. 一句话产品

**simJ**：管理实体 SIM / eSIM 号码（到期、余额、运营商等），支持自建后端 **普通账号同步**，浏览器 **3D 地球** 高亮有号码的国家，同账号网页可看 **完整号码卡片**。

---

## 1. 项目边界（最重要）

| 允许 | 禁止 |
|------|------|
| 只动本仓库 `simj-preview` | 不要动 VPS 上其他项目 |
| 部署只碰 `/opt/simjiang-reminder` | 不要改其它目录 / 端口 / 无关 systemd |
| 服务 `simjiang-reminder`，端口 **8787** | 不要清库（`SIMJ_RESET_DB`）除非明确要求 |

### 线上地址

| 用途 | URL |
|------|-----|
| 用户地球 + 登录门户 | `https://your-domain.example/` 或 `http://<your-server-ip>:8787/` |
| 管理后台 | `https://your-domain.example/admin` 或 `http://<your-server-ip>:8787/admin` |
| 健康检查 | `https://your-domain.example/api/status` 或 `http://<your-server-ip>:8787/api/status` |

SSH / 上传脚本在 `server/simjiang-reminder/`（`deploy.js`、`upload-fix.js`、`upload-admin.js`）。**凭据用环境变量或本机私密脚本，勿写入公开仓库。**

---

## 2. 仓库结构

```text
simj-preview/
├── app/                              # Android（Kotlin + Jetpack Compose）
│   ├── build.gradle.kts              # applicationId=com.sansim.app, minSdk=26
│   └── src/main/
│       ├── AndroidManifest.xml       # cleartext + network_security_config
│       ├── java/com/sansim/app/
│       │   ├── MainActivity.kt       # 主 UI + 普通云同步（大文件）
│       │   ├── SimMapPage.kt         # App 内地图
│       │   ├── data/model/           # App设置 / PhoneNumberRecord / Country
│       │   ├── esim/                 # eSIM 读卡 / 扫码 / 数据库
│       │   ├── i18n/                 # 文案
│       │   ├── update/               # 检查更新
│       │   └── util/                 # SimHub 兼容等
│       ├── res/xml/network_security_config.xml
│       └── assets/                   # 卡面图等
├── server/simjiang-reminder/         # 云端唯一服务
│   ├── server.py                     # HTTP API + 静态文件 + SQLite
│   ├── requirements.txt              # Python 依赖（cryptography）
│   ├── simjiang-reminder.service     # systemd 单元模板
│   ├── web/
│   │   ├── index.html                # 地球壳 + 国家资料面板
│   │   ├── simj-portal.js            # 登录 / 统计 / 号码卡片墙
│   │   ├── simj-admin.js + admin.html
│   │   ├── app/globe-app.js          # 备用/旧地球逻辑
│   │   ├── vendor/globe.gl.js        # 3D 引擎
│   │   ├── data/countries.geojson    # 国家边界
│   │   └── assets/flags/             # 国旗 PNG（按 ISO2）
│   ├── upload-fix.js / upload-admin.js / deploy.js
│   └── data.db（线上路径，不在 git）
├── docs/
│   ├── DEVELOPMENT.md                # 本文
│   └── HANDOFF-CHECKLIST.md
├── README.md
└── gradlew / build.gradle.kts
```

---

## 3. 架构总览

```text
┌──────────────────────┐   HTTP :8787    ┌─────────────────────────────────┐
│  Android App         │ ──────────────► │  server.py (ThreadingHTTPServer)│
│  com.sansim.app      │  Bearer token   │  SQLite data.db                 │
│  本地号码 DB/JSON    │  plain payload  │  按账号保存完整号码 payload     │
│  覆盖统计 / samples  │  coverage       │  coverage = 地图/卡片元数据     │
└──────────────────────┘                 └───────────────┬─────────────────┘
                                                         │ 同端口静态
┌──────────────────────┐                                 ▼
│  浏览器              │ ◄──────── web/index.html + simj-portal.js
│  globe.gl 地球       │            Cookie / 同 token 会话
│  登录后拉 payload    │            同账号可看完整号码卡片
│  + 号码卡片墙        │            地图含中国相关区域显示修正
└──────────────────────┘
```

### 数据分两层（必读）

| 层 | 存什么 | 服务器能否看明文 | 用途 |
|----|--------|------------------|------|
| **payload_json / payload** | 完整号码 + 设置 JSON | **能** | 换机恢复、Web 完整号码卡片 |
| **coverage** | 按国家统计 + samples 卡片字段 | **能**（仅该账号登录后 API 返回） | 地球高亮、卡片摘要、兼容回退 |

> 当前 v7 是普通同步模式：服务器数据库可以读取完整号码。请只部署在可信 VPS 上，公网建议 HTTPS。
> 旧版 `encryptedVault` 仅作为兼容迁移来源；登录成功后会尽量迁移为普通 payload。

---

## 4. 安全模型（当前正确约定）

### 4.1 密钥职责（不要再搞混）

| 材料 | 用途 | 禁止 |
|------|------|------|
| **账号密码** | 登录；拉取/写入当前账号普通 payload；日常恢复 | 不要当作明文配置写入仓库 |
| **privateKey** | **仅**忘记密码时证明身份、重置登录密码 | **禁止**用于同步加密、HTTP 鉴权或日常登录 |
| **session token** | HTTP 鉴权（`Authorization: Bearer` 或 Cookie） | 不当加密密钥 |

### 4.2 普通同步 payload

App 使用 `cloudPlainSyncPayload(records, settings)` 打包：

```json
{
  "mode": "account-plain",
  "payload": {
    "settings": {},
    "records": []
  },
  "coverage": {}
}
```

服务端写入 `encrypted_sync.payload_json`，并继续维护 `coverage_json` 供地图和卡片摘要使用。

兼容说明：

- 服务端仍能尝试读取旧 `encryptedVault`，仅用于登录后迁移旧数据。
- 新写入必须优先使用普通 payload。
- `cloudApiKey` 是 App 历史字段名，当前普通同步不再把它当作同步密钥。

### 4.3 注册私钥（服务器）

- 注册时服务器 `b64url_key(32)` 生成 privateKey，**响应里明文只返回一次**。
- DB 只存 `SHA-256(private_key)` + `private_key_tail`（后 6 位展示用）。
- `POST /api/account/reset-password`：`username + privateKey + newPassword`。

### 4.4 登录密码哈希（服务器）

- PBKDF2-HMAC-SHA256，**210000** 次，仅用于登录密码校验。
- 字段：`password_salt` / `password_hash`（均为 urlsafe base64 无 padding）。

---

## 5. 后端 `server.py` 详解

### 5.1 运行方式

```bash
# 线上默认
export SIMJ_BASE=/opt/simjiang-reminder   # 可选，默认即此
export SIMJ_HOST=0.0.0.0
export SIMJ_PORT=8787
cd /opt/simjiang-reminder
.venv/bin/python server.py
# 线上建议用 systemd: simjiang-reminder.service
```

- 单文件 `ThreadingHTTPServer` + `BaseHTTPRequestHandler`。
- 无 Flask/FastAPI；路由写在 `H.do_GET` / `H.do_POST`。
- 静态站根：`WEB_DIR = BASE/web`。
- 数据库：`BASE/data.db`（SQLite WAL）。

本地调试可把 `SIMJ_BASE` 指到仓库内临时目录，避免污染线上库。

### 5.2 Schema（`init_db`）

| 表 | 作用 |
|----|------|
| `accounts` | 用户：密码哈希、私钥哈希、role、enabled |
| `sessions` | token、account_id、过期时间（默认 7 天） |
| `encrypted_sync` | 每账号一份：payload_json + coverage_json + records_count；保留 envelope 兼容旧数据 |
| `sync_backups` | 每次同步前备份旧 payload/envelope（可恢复/清理） |
| `server_settings` | 键值：是否开放注册、管理员 2FA、兼容旧设置项 |
| `schema_meta` | `version` = `7-plain-sync` |

**首个注册用户** 自动成为 `admin`（见 `register_account`）。

### 5.3 鉴权

1. `Authorization: Bearer <token>`
2. 或 Cookie 会话（Web 登录后 `credentials: "include"`）
3. v7 受保护接口必须有 token；`cloudApiKey` 只是 App 历史字段名，**不再作为 HTTP 鉴权材料**

公开路径（无需登录）：

- `/api/status`
- `/api/public-settings`
- `/api/account/register`
- `/api/account/login`
- `/api/account/reset-password`

### 5.4 API 一览

#### 账号

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/account/register` | body: `{username,password,source?}` → `token` + **`privateKey` 仅一次** |
| POST | `/api/account/login` | → `token` + user（**不**返回 privateKey） |
| POST | `/api/account/reset-password` | `{username,privateKey,newPassword}` |
| POST | `/api/account/logout` | 删 session |
| GET | `/api/account/me` | 当前用户 + **coverage** 摘要 |
| GET | `/api/account/coverage` | 仅 coverage |
| GET | `/api/public-settings` | 是否允许注册等 |

#### 同步（核心）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/sync` | body 见下；保存普通完整号码 payload |
| GET | `/api/sync` 或 `/api/pull` | 返回 `payload` / `vaultPayload` + `coverage` + `records` |

**POST /api/sync body（App 构造）**

```json
{
  "mode": "account-plain",
  "payload": {
    "settings": {},
    "records": [
      {
        "id": "...",
        "countryCode": "+86",
        "countryName": "中国",
        "number": "完整号码",
        "operator": "运营商",
        "expireDate": "2026-01-01",
        "cardType": "prepaid"
      }
    ]
  },
  "coverage": {
    "countries": [
      {
        "iso": "UA",
        "name": "乌克兰",
        "records": 1,
        "esims": 1,
        "samples": [
          {
            "id": "...",
            "number": "完整号码或空",
            "last4": "4567",
            "mask": "...",
            "op": "运营商",
            "esim": true,
            "code": "+380",
            "name": "国家名",
            "flag": "🇺🇦",
            "expire": "2026-01-01",
            "balance": "",
            "cardType": "esim",
            "signal": "在线",
            "note": ""
          }
        ]
      }
    ],
    "countryCount": 1,
    "records": 1,
    "esims": 1,
    "updatedAt": 1234567890
  },
  "records": 1,
  "deviceId": "uuid"
}
```

`normalize_coverage()` 会清洗 samples（最多每国 120 条），**允许完整 number 字段**（仅账号会话可 GET，非公开接口）。

**GET /api/sync 返回重点**

```json
{
  "ok": true,
  "mode": "account-plain",
  "payload": { "settings": {}, "records": [] },
  "vaultPayload": { "settings": {}, "records": [] },
  "coverage": {},
  "records": 0,
  "e2ee": false
}
```

旧字段名 `vaultPayload` 是为了兼容 Web/App 旧读取逻辑；当前内容就是普通 payload。

#### 备份

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/backups?limit=N` | 列表 |
| GET | `/api/backups/{id}` | 详情（payload_json + 旧 envelope 兼容字段） |
| POST | `/api/restore-backup` | `{backupId}` 把备份写回 encrypted_sync |
| POST | `/api/backups/clear` | 清理旧备份 |

#### 管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 可要求 TOTP 第二因子 |
| GET/POST | `/api/admin/settings` | 如 `allowRegistration` |
| GET | `/api/admin/users` | 用户列表 |
| GET/POST | `/api/admin/security` | 2FA 配置动作；旧第二因子兼容 action 会被拒绝并清空 |
| GET | `/api/admin/login-flags` | 登录页是否显示第二因子 |

#### 其它

| 路径 | 说明 |
|------|------|
| GET `/api/status` | 健康、版本 |
| GET `/api/key-info`、`/api/meta` | 兼容旧 App 概览 |
| GET `/api/reminder-status` | 占位（当前提醒仍主要由客户端/后续服务处理） |
| POST `/api/test-telegram` 等 | 软 stub，提示本地执行 |

### 5.5 静态路由

| URL | 文件 |
|-----|------|
| `/` `/user` | `web/index.html` |
| `/admin` | `web/admin.html` |
| `/simj-portal.js` 等 | `web/` 下原样 |

**改前端后务必提高 `index.html` 里 `?v=` 缓存版本**（如 `3d7` → `3d8`），否则用户浏览器仍用旧 JS。

### 5.6 关键后端函数索引（`server.py`）

| 函数 | 作用 |
|------|------|
| `password_hash` / `verify_password` | 登录密码 |
| `private_key_hash` / `reset_password` | 找回密码 |
| `register_account` / `login_account` / `login_admin` | 账号 |
| `create_session` / `session_from_headers` | 会话 |
| `load_coverage` / `normalize_coverage` | 地图元数据 |
| `purge_backups` | 备份数量上限 |
| `H.do_GET` / `H.do_POST` | 全部路由 |
| `init_db` / `main` | 启动 |

---

## 6. Android App 详解

### 6.1 技术栈

- Kotlin 17、Compose Material3、minSdk 26、targetSdk 35
- 包名：`com.sansim.app`
- 版本：`versionName 3.0.24-pre` / `versionCode 3024`
- 网络：`HttpURLConnection`，允许 cleartext 到云端 IP（`network_security_config.xml`）
- 云端默认：开源版不写死维护者服务器；用户在 App 内填写自己的 `https://your-domain.example` 或 `http://<your-server-ip>:8787`。

### 6.2 构建与安装

```powershell
# 必须 JDK 17（不要用 JDK 25 跑 Gradle 8.7）
$env:JAVA_HOME = "C:\Users\...\jdks\ms-17.0.18"
cd simj-preview
.\gradlew.bat assembleDebug

# 模拟器空间不够时先卸载再装
adb uninstall com.sansim.app
adb shell pm trim-caches 2000M
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 6.3 模块职责

| 路径 | 职责 |
|------|------|
| `MainActivity.kt` | 主界面、号码列表、设置、**普通云同步** |
| `SimMapPage.kt` | App 内轻量地球/覆盖统计 |
| `data/model/PhoneNumberRecord.kt` | 单条号码字段 |
| `data/model/AppSettings.kt` | 本地设置 + 云端会话字段 |
| `esim/*` | USB/电话 eSIM、扫码、本地 eSIM 库 |
| `i18n/Translations.kt` | 多语言 key |
| `update/*` | 应用内更新检查 |

### 6.4 `App设置` 云端相关字段

| 字段 | 含义 |
|------|------|
| `cloudUrl` | 服务根 URL |
| `cloudToken` | 登录会话 token |
| `cloudUsername` | 用户名 |
| `cloudApiKey` | 历史字段名；当前普通同步不再作为 HTTP 鉴权或 vault 密钥 |
| `cloudDeviceId` | 设备 UUID |
| `cloudEnabled` / `cloudAutoSync` | 开关 |
| `cloudTelegramEnabled` / `cloudEmailEnabled` | 仅客户端配置；普通同步 payload 另行保存完整号码 |

### 6.5 云同步主流程（App）

```text
注册:
  POST /api/account/register
  → 保存 token
  → 弹窗展示 privateKey（仅备份找回密码用）
  → 若有本地号码 POST /api/sync

登录:
  POST /api/account/login
  → GET /api/sync
  → analyzeCloudSyncResponse(...)
  → 成功则 onCloudRestore；如本地也有数据则合并后 POST /api/sync

同步到云端:
  GET /api/sync 看是否已有云端数据（合并/覆盖对话框）
  POST cloudPlainSyncPayload(records, settings)

从云端恢复:
  GET /api/sync → 读取普通 payload → 写本地
```

### 6.6 关键 App 函数（`MainActivity.kt`）

| 函数 | 作用 |
|------|------|
| `cloudPlainSyncPayload` | records + settings + coverage 打包 POST |
| `cloudCoverage` | 生成 coverage + samples |
| `cloudRequest` / `cloudPost` / `cloudGet` | HTTP（Bearer token、Connection: close、重试 ProtocolException） |
| `analyzeCloudSyncResponse` | 拉包诊断：普通 payload / coverage 回退 |
| `recordsFromCoverageJson` | 从 samples 还原记录（需有完整 number） |
| `mergeRecords` / `mergeCloudSettings` | 多端合并 |
| `设置Page` 内云端 UI | 登录/注册/同步/恢复 |

### 6.7 号码模型 `PhoneNumberRecord`（节选）

`id, countryCode, countryName, flag, number, operator, expireDate, note, balance, eid, smdp, activationCode, startDate, createdAt, activatedAt, longTerm, cycleDays, signalStatus, tags, cardType, sortOrder, ...`

eSIM 判定（coverage）：`cardType/note/tags` 含 esim，或 eid/smdp/activationCode 非空。

---

## 7. Web 前端详解

### 7.1 `index.html`（地球壳）

- 引入 `vendor/globe.gl.js`、内联地球初始化、国家 GeoJSON。
- **国家资料面板** `#profilePanel`：
  - 背景 = 当前国旗（`#panelAmbientBg` 全铺 + 遮罩）
  - ISO / 区号 / 位数 / 坐标
  - `#esimCard`：有 App 同步数据时可点 → 打开号码卡片墙
  - **无**底部「管理员入口」（管理用右上门户或 `/admin`）
- 桥接全局：

| 全局 | 作用 |
|------|------|
| `SIMJ_GLOBE_STATE` | globe 实例、国家 features |
| `SIMJ_SELECT_COUNTRY(iso)` | 飞到国家并打开面板 |
| `SIMJ_SET_COVERAGE_HIGHLIGHT({appIsos,esimIsos,items})` | 高亮有号码国家 |
| `SIMJ_GET_SELECTED_ISO()` | 当前选中 ISO |

### 7.2 `simj-portal.js`（登录与卡片）

- 注入右上浮动面板：登录 / 注册 / 重置密码。
- 登录后：统计三国数字 + **「我的号码」单按钮** → 全屏卡片网格（全部 / eSIM / SIM）。
- 数据源：
  1. `/api/account/me` / `/api/sync` 返回的普通 payload
  2. `coverage.samples` 作为摘要和旧数据回退
- `window.SIMJ_PORTAL.openGallery({iso, filter})`
- `window.SIMJ_PORTAL.getRecordsByIso(iso)`

私钥弹窗文案：**仅找回密码**，与解密无关。

地图边界说明：

- `web/index.html` 和 `web/app/globe-app.js` 会注入 `CN_TIBET` 覆盖层，保证整个西藏区域在产品地图上按中国显示；覆盖层无独立描边，并会遮住底层边界线，避免西藏与中国本体之间出现分割线。旧 `CN_ZANGNAN` 仅作为历史样式兼容。
- 台湾条目使用“中国台湾省”命名，Web/App 均不显示独立国旗。
- 地图供应商/来源：3D 渲染为 `globe.gl`；国家边界为 Natural Earth 110m（本地 `/web/data/countries.geojson`，缺失时使用 globe.gl 示例镜像）；卫星瓦片为 Esri ArcGIS World Imagery；标准地图瓦片为 OpenStreetMap。
- 地图显示、边界覆盖层和 `privateKey` 无关；`privateKey` 只用于忘记密码重置登录密码。
- 地图数据仅用于业务可视化，不作为法定或测绘依据。

### 7.3 管理后台

- `admin.html` + `simj-admin.js`
- 用户列表、开放注册、管理员 2FA/Keepass 等

### 7.4 改 UI 后部署

```bash
# 1. 改 web/index.html 或 simj-portal.js
# 2. 提高 ?v=
# 3. 上传
cd server/simjiang-reminder
node upload-fix.js    # 或自写 sftp 脚本
# 若改了 server.py
systemctl restart simjiang-reminder
```

---

## 8. 端到端场景（给测试用）

### 8.1 新用户

1. App 注册 → 保存 **私钥**（仅防忘密码）→ 添加号码 → 同步到云端。
2. 网页同账号密码登录 → 地球高亮 → 「我的号码」见卡片。
3. 清空 App 数据 → 仅账号密码登录 → 应自动恢复号码。

### 8.2 忘记密码

1. 重置：用户名 + 私钥 + 新密码。
2. 用**新密码**登录；普通同步 payload 不需要重新加密。

### 8.3 网页有统计但看不到完整号码

| 可能原因 | 处理 |
|----------|------|
| 云端只有 coverage，没有普通 payload | 在有本地完整号码的 App 上点「同步到云端」 |
| 旧版密文迁移失败 | 用新版 App 登录并重新同步 |
| 浏览器缓存旧 JS | Ctrl+F5，确认 `simj-portal.js` 版本已更新 |

---

## 9. 开发约定与踩坑

1. **JDK 17** 编译 App；JDK 25 会导致 Gradle 报怪错。
2. 模拟器 **磁盘易满**：`adb uninstall` + `pm trim-caches`。
3. Python HTTP 短连接：App 侧 `Connection: close` + 读流重试，防 `ProtocolException`。
4. 改 Web **必须 bump `?v=`**。
5. **不要**再把 privateKey 写入 `cloudApiKey`；privateKey 只用于重置密码。
6. `cleanCloudApiKey` 名字历史遗留；HTTP 鉴权必须用 `cloudToken`。
7. 服务器普通 payload 可读完整号码；排障时重点看 `payload_json`、`coverage_json` 和 `/api/status.syncMode`。
8. 部署只动 `/opt/simjiang-reminder`。

---

## 10. 建议的后续开发入口

| 需求 | 优先改 |
|------|--------|
| 新 API | `server.py` 路由 + 本文 API 表 |
| 登录/同步逻辑 | `MainActivity.kt` 云端段 |
| 地球样式/国旗面板 | `web/index.html` |
| 号码卡片 UI | `web/simj-portal.js` |
| 管理功能 | `admin.html` + `simj-admin.js` + `/api/admin/*` |
| eSIM 硬件 | `app/.../esim/*` |
| 多语言 | `i18n/Translations.kt` |

---

## 11. 快速命令备忘

```powershell
# 状态
curl https://your-domain.example/api/status

# App Debug
$env:JAVA_HOME="...jdk17..."
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 查线上 DB（SSH 后）
sqlite3 /opt/simjiang-reminder/data.db "SELECT username,role FROM accounts;"
sqlite3 /opt/simjiang-reminder/data.db "SELECT account_id,records_count,length(envelope) FROM encrypted_sync;"
```

---

## 12. 文档维护

- 改安全模型 / API / 目录结构时 **同步更新本文**。
- 交接另见 [HANDOFF-CHECKLIST.md](./HANDOFF-CHECKLIST.md)。
- 产品向简介见仓库根 [README.md](../README.md)。

---

*文档与代码不一致时，以代码为准，并立刻回写本文。*
