/**
 * Fast deploy: upload one tarball of web static package + server.py
 * Scope: ONLY /opt/simjiang-reminder
 */
const fs = require("fs");
const path = require("path");
const { Client } = require("ssh2");

const HOST = process.env.SIMJ_HOST;
const USER = process.env.SIMJ_USER || "root";
const PASS = process.env.SIMJ_PASS;
const REMOTE = "/opt/simjiang-reminder";
const LOCAL = __dirname;
const TAR = process.env.SIMJ_TAR || path.join(process.env.TEMP || "/tmp", "simj-web-static.tgz");

function exec(conn, cmd) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, (err, stream) => {
      if (err) return reject(err);
      let out = "";
      let errOut = "";
      stream.on("data", (d) => (out += d));
      stream.stderr.on("data", (d) => (errOut += d));
      stream.on("close", (code) => resolve({ code, out, errOut }));
    });
  });
}

function put(sftp, localPath, remotePath) {
  return new Promise((resolve, reject) => {
    sftp.fastPut(localPath, remotePath, (err) => (err ? reject(err) : resolve()));
  });
}

async function main() {
  if (!PASS) {
    throw new Error("Set SIMJ_PASS in the environment before deploying");
  }
  if (!HOST) {
    throw new Error("Set SIMJ_HOST in the environment before deploying");
  }
  if (!fs.existsSync(TAR)) {
    console.error("missing tarball:", TAR);
    process.exit(1);
  }
  const conn = new Client();
  await new Promise((resolve, reject) => {
    conn
      .on("ready", resolve)
      .on("error", reject)
      .connect({ host: HOST, port: 22, username: USER, password: PASS, readyTimeout: 30000 });
  });
  console.log("SSH connected");
  const sftp = await new Promise((resolve, reject) => {
    conn.sftp((err, s) => (err ? reject(err) : resolve(s)));
  });

  for (const f of ["server.py", "requirements.txt", "simjiang-reminder.service", "backup.sh"]) {
    const lp = path.join(LOCAL, f);
    if (!fs.existsSync(lp)) continue;
    console.log("upload", f);
    await put(sftp, lp, `${REMOTE}/${f}`);
  }

  console.log("upload tarball", (fs.statSync(TAR).size / 1e6).toFixed(2), "MB");
  await put(sftp, TAR, `${REMOTE}/web-static.tgz`);

  const r = await exec(
    conn,
    `
set -e
mkdir -p ${REMOTE}/web
cd ${REMOTE}/web
tar -xzf ${REMOTE}/web-static.tgz
rm -f ${REMOTE}/web-static.tgz
cp ${REMOTE}/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
systemctl daemon-reload
systemctl restart simjiang-reminder
sleep 1
systemctl is-active simjiang-reminder
curl -sS http://127.0.0.1:8787/api/status; echo
curl -sS -o /dev/null -w 'index:%{http_code} size:%{size_download}\\n' http://127.0.0.1:8787/
curl -sS -o /dev/null -w 'vendor:%{http_code} size:%{size_download}\\n' http://127.0.0.1:8787/vendor/globe.gl.js
curl -sS -o /dev/null -w 'app:%{http_code} size:%{size_download}\\n' http://127.0.0.1:8787/app/globe-app.js
curl -sS -o /dev/null -w 'geo:%{http_code} size:%{size_download}\\n' http://127.0.0.1:8787/data/countries.geojson
curl -sS -o /dev/null -w 'meta:%{http_code} size:%{size_download}\\n' http://127.0.0.1:8787/data/offline-country-meta.json
du -sh ${REMOTE}/web ${REMOTE}/web/vendor ${REMOTE}/web/app ${REMOTE}/web/data ${REMOTE}/web/assets 2>/dev/null || true
echo SCOPE_OK only simjiang-reminder
`
  );
  console.log(r.out || "");
  if (r.errOut) console.log("stderr:", r.errOut);
  console.log(r.code === 0 ? "DEPLOY OK" : "DEPLOY FAIL " + r.code);
  conn.end();
  process.exit(r.code || 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
