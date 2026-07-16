# SIMJ 全栈开发文档（接手必读）

> 目标：不翻聊天记录也能改 **Android App / 3D 地球 Web / Python 云端 / 管理后台**。
> 最后对齐：后端 `v6-e2ee-globe-admin2fa` · App `3.0.24-pre` · Web 缓存 `?v=3d7`
> 仓库根目录：`simj-preview/`

---

## 0. 一句话产品

**simJ**：管理实体 SIM / eSIM 号码（到期、余额、运营商等），支持 **端到端加密云同步**，浏览器 **3D 地球** 高亮有号码的国家，同账号网页可看 **号码卡片**（完整号码仅登录用户可见）。

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
│       │   ├── MainActivity.kt       # 主 UI + 云同步 E2EE（大文件）
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
│   ├── requirements.txt              # 标准库为主，无强制第三方
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
│  密码派生 AES-GCM    │  encryptedVault │  永不解密号码正文               │
│  本地号码 DB/JSON    │  coverage       │  coverage = 地图/卡片元数据     │
└──────────────────────┘                 └───────────────┬─────────────────┘
                                                         │ 同端口静态
┌──────────────────────┐                                 ▼
│  浏览器              │ ◄──────── web/index.html + simj-portal.js
│  globe.gl 地球       │            Cookie / 同 token 会话
│  登录后拉 coverage   │            可选：WebCrypto 解 vault
│  + 号码卡片墙        │
└──────────────────────┘
```

### 数据分两层（必读）

| 层 | 存什么 | 服务器能否看明文 | 用途 |
|----|--------|------------------|------|
| **encryptedVault** | 完整号码 + 设置 JSON，AES-256-GCM | **否** | 换机恢复、权威备份 |
| **coverage** | 按国家统计 + samples 卡片字段 | **能**（仅该账号登录后 API 返回） | 地球高亮、网页号码卡片 |

> 网页地图能显示「有 3 国 / 3 张卡」**不代表** vault 已用密码解开。
> 恢复完整记录依赖：登录密码派生密钥解密 vault（或 samples 里有完整号码时的卡片回退）。

---

## 4. 安全模型（当前正确约定）

### 4.1 密钥职责（不要再搞混）

| 材料 | 用途 | 禁止 |
|------|------|------|
| **账号密码** | 登录；**派生 vault 加解密密钥**；日常恢复 | — |
| **privateKey** | **仅**忘记密码时证明身份、重置登录密码 | **禁止**用于 vault 加解密 |
| **session token** | HTTP 鉴权（`Authorization: Bearer` 或 Cookie） | 不当加密密钥 |

### 4.2 密码派生 vault 密钥（App / Web 必须一致）

```text
AAD        = UTF-8("simj:e2ee:v1")
salt       = SHA-256(UTF-8("simj:e2ee:v1:" + username.trim().lowercase()))[0:16]
secret_raw = PBKDF2-HMAC-SHA256(password, salt, iterations=310000, dkLen=32)
secret_b64 = Base64URL(secret_raw)   # 无 padding，与 Android Base64.URL_SAFE|NO_PADDING 一致

AES-256-GCM:
  key   = secret_raw（32 bytes）
  nonce = 12 随机字节
  AAD   = "simj:e2ee:v1"
  输出  ciphertext + tag(16)
```

App 实现位置：`MainActivity.kt`

- `deriveSimjCloudSecret(username, password)`
- `cloudEncryptPayload` / `cloudDecryptPayload` / `analyzeCloudSyncResponse`
- 登录成功后：`cloudApiKey = pwdSecret`（此处字段名历史遗留，**实际是密码派生密钥**，不是 privateKey）

加密包 `mode` 字段：`app-e2ee-pwd`（新）。
旧数据可能为 `app-e2ee`（若曾误用私钥当密钥，密码无法解开，需在有本地号码的设备上 **同步到云端** 用密码重写）。

### 4.3 注册私钥（服务器）

- 注册时服务器 `b64url_key(32)` 生成 privateKey，**响应里明文只返回一次**。
- DB 只存 `SHA-256(private_key)` + `private_key_tail`（后 6 位展示用）。
- `POST /api/account/reset-password`：`username + privateKey + newPassword`。

### 4.4 登录密码哈希（服务器）

- PBKDF2-HMAC-SHA256，**210000** 次（注意：与 vault 的 310000 **不同**，不要混用）。
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
python3 server.py
# 或 systemd: simjiang-reminder.service
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
| `encrypted_sync` | 每账号一份：envelope 密文 + coverage_json + records_count |
| `sync_backups` | 每次同步前备份旧 envelope（可恢复/清理） |
| `server_settings` | 键值：是否开放注册、管理员 2FA、兼容旧设置项 |
| `schema_meta` | `version` = `6-e2ee-admin2fa` |

**首个注册用户** 自动成为 `admin`（见 `register_account`）。

### 5.3 鉴权

1. `Authorization: Bearer <token>`
2. 或 Cookie 会话（Web 登录后 `credentials: "include"`）
3. v6 受保护接口必须有 token；`cloudApiKey` 只是 App 历史字段名，实际保存密码派生的 vault 密钥，**不再作为 HTTP 鉴权材料**

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
| POST | `/api/sync` | body 见下；服务器**不解密** |
| GET | `/api/sync` 或 `/api/pull` | 返回 `encryptedVault` + `coverage` + `records` |

**POST /api/sync body（App 构造）**

```json
{
  "encryptedVault": {
    "v": 2,
    "mode": "app-e2ee-pwd",
    "alg": "AES-256-GCM",
    "kdf": "PBKDF2-HMAC-SHA256",
    "iter": 310000,
    "salt": "<b64u username-salt>",
    "nonce": "<b64u 12bytes>",
    "ciphertext": "<b64u>",
    "tag": "<b64u 16bytes>",
    "updatedAt": 1234567890
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

**vault 明文（App 加密前）**

```json
{
  "settings": { /* App设置，云端密钥字段会清空后再打包 */ },
  "records": [ /* PhoneNumberRecord 数组 */ ]
}
```

#### 备份

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/backups?limit=N` | 列表 |
| GET | `/api/backups/{id}` | 详情（仍是密文 envelope） |
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
| GET `/api/reminder-status` | 占位（E2EE 后提醒在客户端） |
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
| `MainActivity.kt` | 主界面、号码列表、设置、**全部云同步/E2EE** |
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
| `cloudApiKey` | **密码派生的 vault 密钥**（历史字段名；不是 API Key，勿存 privateKey） |
| `cloudDeviceId` | 设备 UUID |
| `cloudEnabled` / `cloudAutoSync` | 开关 |
| `cloudTelegramEnabled` / `cloudEmailEnabled` | 仅客户端配置；服务器 E2EE 下不读号码 |

### 6.5 云同步主流程（App）

```text
注册:
  POST /api/account/register
  → 保存 token
  → cloudApiKey = deriveSimjCloudSecret(user, pass)
  → 弹窗展示 privateKey（仅备份找回密码用）
  → 若有本地号码 POST /api/sync

登录:
  POST /api/account/login
  → cloudApiKey = deriveSimjCloudSecret(user, pass)
  → GET /api/sync
  → analyzeCloudSyncResponse(..., password=登录密码)
  → 成功则 onCloudRestore + 可选用密码重加密再 POST /api/sync

同步到云端:
  GET /api/sync 看是否已有云端数据（合并/覆盖对话框）
  POST cloudEncryptedPayload(records, settings)

从云端恢复:
  GET /api/sync → 密码密钥解密 → 写本地
```

### 6.6 关键 App 函数（`MainActivity.kt`）

| 函数 | 作用 |
|------|------|
| `deriveSimjCloudSecret` | 密码 → vault 密钥 |
| `passwordVaultSecrets` | 多种密码派生候选（兼容旧 salt） |
| `cloudEncryptPayload` / `cloudDecryptPayload` | AES-GCM |
| `cloudCoverage` | 生成 coverage + samples |
| `cloudEncryptedPayload` | vault + coverage 打包 POST |
| `cloudRequest` / `cloudPost` / `cloudGet` | HTTP（Bearer token、Connection: close、重试 ProtocolException） |
| `analyzeCloudSyncResponse` | 拉包诊断：有无密文 / 解密成败 / coverage 回退 |
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
  1. `coverage.samples`（登录会话）
  2. 可选 WebCrypto 解密 vault（`sessionStorage` 存派生密钥）
- `window.SIMJ_PORTAL.openGallery({iso, filter})`
- `window.SIMJ_PORTAL.getRecordsByIso(iso)`

私钥弹窗文案：**仅找回密码**，与解密无关。

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
# 重启 python 进程 / systemd
```

---

## 8. 端到端场景（给测试用）

### 8.1 新用户

1. App 注册 → 保存 **私钥**（仅防忘密码）→ 添加号码 → 同步到云端。
2. 网页同账号密码登录 → 地球高亮 → 「我的号码」见卡片。
3. 清空 App 数据 → 仅账号密码登录 → 应自动恢复号码。

### 8.2 忘记密码

1. 重置：用户名 + 私钥 + 新密码。
2. 用**新密码**登录；vault 若仍是旧密码加密，需在有本地数据的设备同步一次用新密码重写。

### 8.3 网页有统计、App 解不开

| 可能原因 | 处理 |
|----------|------|
| 密码错误 | 重新登录 |
| 旧密文曾误用私钥加密 | 在有本地号码的设备登录后点「同步到云端」用密码重写 |
| coverage 有、samples 无数、vault 密钥不对 | 同上 |

---

## 9. 开发约定与踩坑

1. **JDK 17** 编译 App；JDK 25 会导致 Gradle 报怪错。
2. 模拟器 **磁盘易满**：`adb uninstall` + `pm trim-caches`。
3. Python HTTP 短连接：App 侧 `Connection: close` + 读流重试，防 `ProtocolException`。
4. 改 Web **必须 bump `?v=`**。
5. **不要**再把 privateKey 写入 `cloudApiKey` 或拿去 AES。
6. `cleanCloudApiKey` 名字历史遗留，用于清洗 vault 密钥/privateKey 输入；HTTP 鉴权必须用 `cloudToken`。
7. 服务器 **永不** 解密 vault；排障只看 envelope 结构与 coverage。
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
