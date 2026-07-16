# SIMJ 云同步后端部署指南

当前后端是 v6 E2EE 架构：服务器保存账号、session、密文 vault、coverage 元数据和备份；完整号码由 App/Web 使用账号密码在客户端解密。

本文档不包含维护者的服务器地址或凭据。部署时请替换为你自己的 VPS IP、域名和 SSH 登录方式。

## 推荐先读

完整文档：

- [server/simjiang-reminder/README.md](server/simjiang-reminder/README.md)
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

## VPS 推荐

| 场景 | 配置 |
| --- | --- |
| 个人自用 | 1 vCPU / 1 GB RAM / 10 GB SSD |
| 朋友小范围使用 | 1-2 vCPU / 2 GB RAM / 20 GB SSD |
| 更多用户或长期备份 | 2 vCPU / 2-4 GB RAM / 40 GB+ SSD |

建议系统：Debian 12 或 Ubuntu 22.04/24.04。

建议网络：有 IPv4；生产环境使用域名 + HTTPS。

建议安全组：测试可开放 8787；正式环境只开放 80/443，由 Nginx/Caddy 反代到本机 8787。

## 快速部署

```bash
sudo apt update
sudo apt install -y python3 rsync

sudo install -d -m 700 /opt/simjiang-reminder
sudo rsync -a --delete \
  --exclude 'node_modules' \
  server/simjiang-reminder/server.py \
  server/simjiang-reminder/backup.sh \
  server/simjiang-reminder/requirements.txt \
  server/simjiang-reminder/web \
  /opt/simjiang-reminder/

sudo chmod +x /opt/simjiang-reminder/backup.sh
sudo cp server/simjiang-reminder/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
sudo systemctl daemon-reload
sudo systemctl enable --now simjiang-reminder
```

验证：

```bash
curl http://127.0.0.1:8787/api/status
curl http://127.0.0.1:8787/api/public-settings
```

公网访问：

```text
http://<your-server-ip>:8787/
http://<your-server-ip>:8787/admin
```

## App 端配置

在 App 的云同步设置里填写你自己的服务地址：

```text
https://your-domain.example
```

或测试环境：

```text
http://<your-server-ip>:8787
```

然后注册/登录云同步账号。注册时显示的 `privateKey` 只用于忘记密码重置，不是 API Key，也不是 vault 解密密钥。

## 注意事项

1. 不要把真实服务器 IP、SSH 密码、token、证书私钥、`data.db` 或备份文件提交到 Git。
2. 服务器不解密完整号码，管理员后台也不显示用户私钥尾号。
3. 生产环境建议 HTTPS；HTTP 只适合测试或内网。
4. 修改用户名不会重写 vault。新版 App 会读取加密包内的 salt 尝试解密旧数据。
5. 重置密码不会自动重加密旧 vault；需要在仍有本地数据的设备上重新同步。
6. 删除账户会删除服务器上的密文 vault、备份和 session，服务器无法恢复明文号码。

## 运维命令

```bash
journalctl -u simjiang-reminder -f
systemctl restart simjiang-reminder
bash /opt/simjiang-reminder/backup.sh
sqlite3 /opt/simjiang-reminder/data.db "SELECT username,role,enabled FROM accounts;"
```
