# SIMJ Self-Hosted Cloud Backend

SIMJ 的自建云同步后端：账号登录、端到端加密 vault 保存、备份、管理后台和 3D 地球页面。

这个目录不包含任何项目维护者的服务器地址、账号或密码。部署时请使用你自己的 VPS、域名和凭据。

## 核心原则

- 账号密码用于登录，也用于在 App/Web 端派生 vault 加密密钥。
- 注册时返回的一次性 `privateKey` 只用于忘记密码时重置登录密码。
- 服务器只保存密码哈希、私钥哈希、session、密文 `encryptedVault` 和 coverage 元数据。
- 服务器不解密完整号码，不在管理员后台显示用户私钥尾号。

## VPS 推荐配置

| 场景 | 推荐配置 |
| --- | --- |
| 个人/小团队，1-20 个账号 | 1 vCPU、1 GB RAM、10 GB SSD |
| 公开给朋友使用，20-100 个账号 | 1-2 vCPU、2 GB RAM、20 GB SSD |
| 大量地图访问或备份较多 | 2 vCPU、2-4 GB RAM、40 GB+ SSD |

最低可以跑在 512 MB 内存的小机子上，但 1 GB RAM 更稳。建议选择 Debian 12 或 Ubuntu 22.04/24.04，Python 3.10+，带 IPv4，月流量 100 GB 以上。生产环境建议绑定域名并启用 HTTPS。

## 文件说明

```text
server.py                    后端 API + 静态文件服务
web/                         用户地图、登录门户、管理后台
backup.sh                    SQLite 备份脚本
simjiang-reminder.service    systemd 服务模板
requirements.txt             Python 运行依赖，当前包含 cryptography
deploy*.js / upload*.js      可选 SSH 部署脚本，必须通过环境变量传入主机信息
```

## 部署总览

推荐把后端部署到 VPS 的固定目录：

```text
/opt/simjiang-reminder
```

默认监听端口：

```text
0.0.0.0:8787
```

部署完成后会得到两个页面：

```text
http://<your-server-ip>:8787/
http://<your-server-ip>:8787/admin
```

生产环境建议再绑定域名并用 HTTPS：

```text
https://your-domain.example/
https://your-domain.example/admin
```

完整流程是：

1. 准备 VPS。
2. 把仓库代码放到 VPS。
3. 复制 `server/simjiang-reminder` 到 `/opt/simjiang-reminder`。
4. 创建 Python venv 并安装依赖。
5. 安装 systemd 服务并启动。
6. 验证 API、网页和管理后台。
7. 在 App 里填写你的服务地址。

## 1. 准备 VPS

在 VPS 上执行：

```bash
sudo apt update
sudo apt install -y git rsync curl python3 python3-venv python3-pip
```

放行端口：

- 测试直连：放行 `8787/tcp`。
- 正式 HTTPS：只放行 `80/tcp` 和 `443/tcp`，让 Nginx/Caddy 反代到本机 `8787`。

## 2. 获取代码

方式 A：在 VPS 上直接克隆你的 fork。

```bash
cd ~
git clone https://github.com/<your-github-user>/simj-preview.git
cd ~/simj-preview
```

方式 B：从本地上传仓库到 VPS。只要最终 VPS 上有完整仓库目录即可，下面都假设当前目录是仓库根目录。

## 3. 安装后端文件

这些命令只复制后端需要的文件，不会清空线上 `data.db` 或 `backups/`：

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

## 4. 安装 Python 依赖

后端使用标准库提供 HTTP 服务，并通过 `cryptography` 支持 Web 登录时的 vault 解密兼容逻辑。用 venv 安装最干净：

```bash
sudo python3 -m venv /opt/simjiang-reminder/.venv
sudo /opt/simjiang-reminder/.venv/bin/python -m pip install --upgrade pip
sudo /opt/simjiang-reminder/.venv/bin/pip install -r /opt/simjiang-reminder/requirements.txt
```

## 5. 安装并启动 systemd 服务

```bash
sudo cp /opt/simjiang-reminder/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
sudo systemctl daemon-reload
sudo systemctl enable --now simjiang-reminder
```

检查运行状态：

```bash
sudo systemctl status simjiang-reminder --no-pager
curl http://127.0.0.1:8787/api/status
curl http://127.0.0.1:8787/api/public-settings
```

正常时 `/api/status` 会返回 JSON，服务状态应为 `active (running)`。

## 6. 打开网页

测试环境可以直接访问：

```text
http://<your-server-ip>:8787/
http://<your-server-ip>:8787/admin
```

如果打不开，先检查三件事：

```bash
sudo systemctl status simjiang-reminder --no-pager
sudo journalctl -u simjiang-reminder -n 80 --no-pager
curl http://127.0.0.1:8787/api/status
```

本机 `curl` 正常但公网打不开，通常是 VPS 防火墙或云厂商安全组没放行端口。

## 7. 配置 HTTPS

正式使用建议走 HTTPS。先安装 Nginx 和 Certbot：

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.example
```

Nginx 反代配置示例：

```nginx
server {
    listen 80;
    server_name your-domain.example;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.example;

    ssl_certificate     /etc/letsencrypt/live/your-domain.example/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.example/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8787;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

使用反代后，建议只让后端监听本机地址：

```bash
sudo systemctl edit simjiang-reminder
```

填入：

```ini
[Service]
Environment=SIMJ_HOST=127.0.0.1
```

然后重启：

```bash
sudo systemctl daemon-reload
sudo systemctl restart simjiang-reminder
```

## 8. App 端怎么填

在 App 里打开：

```text
设置 -> 云端数据与备份 / 云同步
```

服务地址填写：

```text
https://your-domain.example
```

测试直连可以填：

```text
http://<your-server-ip>:8787
```

不要填写 `/admin`，也不要填写 `/api/status`，只填服务根地址。

## 9. 首次使用

1. 打开 `https://your-domain.example/` 或 `http://<your-server-ip>:8787/`。
2. 注册第一个账号。第一个账号会自动成为 `owner`。
3. 立即保存注册时显示的一次性 `privateKey`。它只用于忘记密码重置。
4. 在 App 云同步里填写同一个服务地址，登录同一账号。
5. 点击同步。App 会上传密文 `encryptedVault` 和 coverage 元数据。
6. 在 Web 登录同一账号，能看到国家覆盖和号码卡片，说明 App/Web/后端链路正常。

## 10. 后续更新

以后更新后端时，重新进入仓库根目录，拉取最新版，再复制文件并重启服务：

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

## 可选：使用部署脚本

部署脚本适合你完全信任当前仓库和目标 VPS 时使用。不要把 SSH 密码写进脚本或提交到 Git。

先在本机或 VPS 的仓库目录安装脚本依赖：

```bash
cd server/simjiang-reminder
npm install
```

再通过环境变量传入你自己的服务器信息：

```bash
export SIMJ_HOST="<your-server-ip-or-domain>"
export SIMJ_USER="<your-ssh-user>"
export SIMJ_PASS="<your-ssh-password>"

node deploy.js
```

脚本只会操作 `/opt/simjiang-reminder` 和 `simjiang-reminder` 这个 systemd 服务。开源仓库中不应出现真实 IP、SSH 用户密码、token、证书私钥或数据库文件。

## 管理后台

后台地址：

```text
https://your-domain.example/admin
```

管理员可以：

- 开关新用户注册。
- 查看账户列表、角色、启用状态和同步统计。
- 修改管理员用户名和密码。
- 停用、启用、删除账户。
- 开启/关闭管理员 2FA。

管理员不能：

- 查看用户完整号码明文。
- 查看用户私钥或私钥尾号。
- 解密用户的 `encryptedVault`。

## 备份与数据

运行数据在：

```text
/opt/simjiang-reminder/data.db
/opt/simjiang-reminder/backups/
```

手动备份：

```bash
bash /opt/simjiang-reminder/backup.sh
```

建议：

- 定期把 `data.db` 和 `backups/` 复制到另一台机器或对象存储。
- 开源前不要提交 `data.db`、`*.db-wal`、`*.db-shm`、备份包、日志和 SSH 密钥。
- 删除账户会删除该账户的云端密文、备份和 session，不能从服务器恢复明文号码。

## 安全注意事项

- 生产环境建议 HTTPS；Android 可访问 HTTP，但公网明文传输不推荐。
- 不要把 `SIMJ_PASS`、SSH key、域名证书、数据库、真实服务器 IP 写进仓库。
- `8787` 直连适合测试；正式服务建议 Nginx/Caddy 反代。
- 修改用户名不会解密或重写用户 vault。新版 App 会读取加密包里的 salt 作为解密候选；旧版本 App 改名后可能需要升级再恢复。
- 忘记密码后用 `privateKey` 重置的是登录密码，不会重加密旧 vault；需要在仍有本地号码的设备上重新同步一次。

## 常用运维命令

```bash
journalctl -u simjiang-reminder -f
systemctl restart simjiang-reminder
systemctl stop simjiang-reminder
systemctl show simjiang-reminder --property=ActiveState,SubState,MainPID,NRestarts
sqlite3 /opt/simjiang-reminder/data.db "SELECT username,role,enabled FROM accounts;"
```

## 故障排查

| 现象 | 检查 |
| --- | --- |
| 无法访问网页 | `systemctl status`、防火墙、VPS 安全组、端口 8787 |
| HTTPS 后 App 登录失败 | 反代是否转发到 `127.0.0.1:8787`，证书是否有效 |
| App 显示云端有数据但无法恢复 | 登录密码是否正确；旧 vault 是否用旧密码加密；必要时在有本地数据的设备重新同步 |
| 管理后台无法登录 | 用户是否为 `owner/admin`，2FA 是否开启，系统时间是否准确 |
| 数据库异常 | 先停服务，备份现有文件，再从 `backups/` 恢复 |
