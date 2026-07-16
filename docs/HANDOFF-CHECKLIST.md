# 接手检查清单（5 分钟）

## 1. 范围

- [ ] 只改本仓库 `simj-preview`
- [ ] 部署只碰 `/opt/simjiang-reminder` 与 `simjiang-reminder` 服务（端口 **8787**）

## 2. 读文档

- [ ] 精读 [DEVELOPMENT.md](./DEVELOPMENT.md)（架构、E2EE、API、App、Web、踩坑）

## 3. 安全模型（口头能复述）

- [ ] **密码** = 登录 + vault 加解密
- [ ] **私钥** = 仅忘记密码重置（服务器只存 hash）
- [ ] 服务器 **永不** 解密 `encryptedVault`
- [ ] `coverage` = 地图/卡片元数据（登录用户可见）

## 4. 验证线上

```bash
curl -sS https://your-domain.example/api/status
curl -sS https://your-domain.example/api/public-settings
```

- [ ] 地球页 Ctrl+F5，`index.html` / `simj-portal.js` 的 `?v=` 为当前版本
- [ ] 点国家：国旗整块背景、**无**底部管理员入口
- [ ] 登录后「我的号码」卡片墙；App 同步后高亮国家

## 5. 本地改代码后

| 改动 | 动作 |
|------|------|
| `web/index.html` / `simj-portal.js` | 提高 `?v=` → 上传 `web/` |
| `server.py` | 上传后 **重启** python/systemd |
| App | JDK **17** `gradlew assembleDebug` → adb install |

## 6. 禁止事项

- [ ] 不用 privateKey 做 AES / 不写进 `cloudApiKey`
- [ ] App 登录不强制填私钥
- [ ] 不 `SIMJ_RESET_DB` 除非明确清库
- [ ] 不部署到 VPS 其它目录

## 7. 常见故障

| 现象 | 方向 |
|------|------|
| 网页有统计、App 说解不开 | 密码密钥是否匹配；有本地号则再「同步到云端」 |
| 注册 ProtocolException | HTTP/1.1 + Connection close；服务是否在 8787 |
| 模拟器装不上 | `adb uninstall` + `pm trim-caches` |
| Gradle 秒失败 `25.0.2` | 换 JDK 17 |
