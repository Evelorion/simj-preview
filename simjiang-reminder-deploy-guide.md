# DsimJ 云同步后端部署指南

这个后端用于 App/Web 云同步：账号登录、端到端加密 vault 保存、coverage 元数据、备份、管理后台和 Web 地球页。

本文档不包含维护者服务器地址或凭据。所有 IP、域名、SSH 用户名和密码都要换成你自己的。

更完整说明见：

- [server/simjiang-reminder/README.md](server/simjiang-reminder/README.md)
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

## VPS 推荐

| 场景 | 配置 |
| --- | --- |
| 个人自用 | 1 vCPU / 1 GB RAM / 10 GB SSD |
| 朋友小范围使用 | 1-2 vCPU / 2 GB RAM / 20 GB SSD |
| 更多用户或长期备份 | 2 vCPU / 2-4 GB RAM / 40 GB+ SSD |

建议系统：Debian 12 或 Ubuntu 22.04/24.04。

建议网络：

- 测试：开放 `8787/tcp`。
- 正式：开放 `80/tcp` 和 `443/tcp`，用 HTTPS 反向代理到本机 `8787`。

## 快速部署

以下命令在 VPS 上执行。先安装系统依赖：

```bash
sudo apt update
sudo apt install -y git rsync curl python3 python3-venv python3-pip
```

再把仓库克隆到 VPS：

```bash
cd ~
git clone https://github.com/<your-github-user>/simj-preview.git
cd ~/simj-preview
```

复制后端文件到固定目录：

```bash
sudo install -d -m 750 /opt/simjiang-reminder
sudo install -d -m 750 /opt/simjiang-reminder/web

sudo rsync -a \
  server/simjiang-reminder/server.py \
  server/simjiang-reminder/backup.sh \
  server/simjiang-reminder/requirements.txt \
  server/simjiang-reminder/simjiang-reminder.service \
  /opt/simjiang-reminder/

sudo rsync -a --delete \
  server/simjiang-reminder/web/ \
  /opt/simjiang-reminder/web/

sudo chmod +x /opt/simjiang-reminder/backup.sh
```

安装 Python 依赖：

```bash
sudo python3 -m venv /opt/simjiang-reminder/.venv
sudo /opt/simjiang-reminder/.venv/bin/python -m pip install --upgrade pip
sudo /opt/simjiang-reminder/.venv/bin/pip install -r /opt/simjiang-reminder/requirements.txt
```

安装并启动服务：

```bash
sudo cp /opt/simjiang-reminder/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
sudo systemctl daemon-reload
sudo systemctl enable --now simjiang-reminder
```

验证：

```bash
sudo systemctl status simjiang-reminder --no-pager
curl http://127.0.0.1:8787/api/status
curl http://127.0.0.1:8787/api/public-settings
```

测试访问：

```text
http://<your-server-ip>:8787/
http://<your-server-ip>:8787/admin
```

## HTTPS 正式部署

正式使用建议绑定域名：

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.example
```

Nginx 反代目标是：

```text
http://127.0.0.1:8787
```

使用 HTTPS 后，App 里填写：

```text
https://your-domain.example
```

不要填写 `/admin` 或 `/api/status`。

## App 端配置

打开 App：

```text
设置 -> 云端数据与备份 / 云同步
```

填写你的服务地址：

```text
https://your-domain.example
```

测试直连可以填：

```text
http://<your-server-ip>:8787
```

然后注册或登录云同步账号。注册时显示的 `privateKey` 只用于忘记密码重置，不是 API Key，也不是 vault 解密密钥。

## 后续更新

```bash
cd ~/simj-preview
git pull

sudo rsync -a \
  server/simjiang-reminder/server.py \
  server/simjiang-reminder/backup.sh \
  server/simjiang-reminder/requirements.txt \
  server/simjiang-reminder/simjiang-reminder.service \
  /opt/simjiang-reminder/

sudo rsync -a --delete \
  server/simjiang-reminder/web/ \
  /opt/simjiang-reminder/web/

sudo /opt/simjiang-reminder/.venv/bin/pip install -r /opt/simjiang-reminder/requirements.txt
sudo cp /opt/simjiang-reminder/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
sudo systemctl daemon-reload
sudo systemctl restart simjiang-reminder
curl http://127.0.0.1:8787/api/status
```

不要删除 `/opt/simjiang-reminder/data.db` 和 `/opt/simjiang-reminder/backups/`，除非你明确要清空所有云端账号和数据。

## 常用运维

```bash
sudo journalctl -u simjiang-reminder -f
sudo systemctl restart simjiang-reminder
bash /opt/simjiang-reminder/backup.sh
sqlite3 /opt/simjiang-reminder/data.db "SELECT username,role,enabled FROM accounts;"
```

## 注意事项

1. 不要把真实服务器 IP、SSH 密码、token、证书私钥、`data.db` 或备份文件提交到 Git。
2. 服务器不解密完整号码，管理员后台也不显示用户私钥尾号。
3. 生产环境建议 HTTPS；HTTP 只适合测试或内网。
4. 修改用户名不会重写 vault。新版 App 会读取加密包内的 salt 尝试解密旧数据。
5. 重置密码不会自动重加密旧 vault；需要在仍有本地数据的设备上重新同步。
6. 删除账户会删除服务器上的密文 vault、备份和 session，服务器无法恢复明文号码。
