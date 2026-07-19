/**
 * Deploy simJ E2EE cloud to VPS via SSH/SFTP.
 *
 * Scope (do not touch other projects):
 *   - only /opt/simjiang-reminder
 *   - only systemd unit simjiang-reminder
 *   - only port 8787
 *
 * Usage:
 *   node deploy.js
 *   SIMJ_SKIP_INDEX=1 node deploy.js   # skip 76MB globe HTML (default for patches)
 *   SIMJ_RESET_DB=1 node deploy.js     # archive data.db then fresh start
 */
const fs = require("fs");
const path = require("path");
const { Client } = require("ssh2");

const HOST = process.env.SIMJ_HOST;
const USER = process.env.SIMJ_USER;
const PASS = process.env.SIMJ_PASS;
const REMOTE = "/opt/simjiang-reminder";
const LOCAL = __dirname;
// Default skip huge index.html for iterative patches; set SIMJ_SKIP_INDEX=0 to force upload.
// After globe optimize, re-upload index with SIMJ_SKIP_INDEX=0.
const SKIP_INDEX = process.env.SIMJ_SKIP_INDEX !== "0";

const FILES = [
  "server.py",
  "requirements.txt",
  "simjiang-reminder.service",
  "backup.sh",
];

// Always deploy the slim shell + vendor + geo. SKIP_INDEX only skips legacy huge bak.
const WEB_FILES = [
  "admin.html",
  "simj-portal.js",
  "index.html",
  "vendor-globe.js",
  "countries-lite.json",
];

function exec(conn, cmd, timeoutMs = 120000) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, { timeout: timeoutMs }, (err, stream) => {
      if (err) return reject(err);
      let out = "";
      let errOut = "";
      stream.on("data", (d) => (out += d.toString()));
      stream.stderr.on("data", (d) => (errOut += d.toString()));
      stream.on("close", (code) => resolve({ code, out, errOut }));
    });
  });
}

function sftpUpload(sftp, localPath, remotePath) {
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
  if (!USER) {
    throw new Error("Set SIMJ_USER in the environment before deploying");
  }
  const conn = new Client();
  await new Promise((resolve, reject) => {
    conn
      .on("ready", resolve)
      .on("error", reject)
      .connect({ host: HOST, port: 22, username: USER, password: PASS, readyTimeout: 30000 });
  });
  console.log("SSH connected");

  // SIMJ_RESET_DB=1 will archive the existing database before deploy (fresh schema).
  // Default is to keep data so iterative deploys do not wipe accounts/sync.
  const resetDb = process.env.SIMJ_RESET_DB === "1";
  let r = await exec(
    conn,
    `
set -e
mkdir -p ${REMOTE}/web ${REMOTE}/legacy-backup
systemctl stop simjiang-reminder 2>/dev/null || true
if [ "${resetDb ? "1" : "0"}" = "1" ] && [ -f ${REMOTE}/data.db ]; then
  ts=$(date +%Y%m%d%H%M%S)
  mv ${REMOTE}/data.db ${REMOTE}/legacy-backup/data.db.$ts || true
  rm -f ${REMOTE}/data.db-wal ${REMOTE}/data.db-shm || true
  echo "RESET_DB archived data.db.$ts"
else
  echo "KEEP_DB"
fi
which python3
python3 --version
`
  );
  console.log(r.out || r.errOut);

  const sftp = await new Promise((resolve, reject) => {
    conn.sftp((err, s) => (err ? reject(err) : resolve(s)));
  });

  for (const f of FILES) {
    const lp = path.join(LOCAL, f);
    if (!fs.existsSync(lp)) {
      console.log("skip missing", f);
      continue;
    }
    const rp = `${REMOTE}/${f}`;
    console.log("upload", f);
    await sftpUpload(sftp, lp, rp);
  }

  for (const f of WEB_FILES) {
    const lp = path.join(LOCAL, "web", f);
    if (!fs.existsSync(lp)) {
      console.log("skip missing web/", f);
      continue;
    }
    const rp = `${REMOTE}/web/${f}`;
    const size = fs.statSync(lp).size;
    console.log(`upload web/${f} (${(size / 1024 / 1024).toFixed(1)} MB)`);
    await sftpUpload(sftp, lp, rp);
  }

  // remove obsolete admin js only (keep countries-lite for the slim globe)
  await exec(conn, `rm -f ${REMOTE}/web/simj-admin.js || true`);

  // Only manage simjiang-reminder unit — never restart nginx/docker/other app units.
  r = await exec(
    conn,
    `
set -e
chmod 755 ${REMOTE}
chmod 644 ${REMOTE}/server.py
chmod +x ${REMOTE}/backup.sh 2>/dev/null || true
python3 -m venv ${REMOTE}/.venv
${REMOTE}/.venv/bin/python -m pip install --upgrade pip
${REMOTE}/.venv/bin/pip install -r ${REMOTE}/requirements.txt
# unit file only for this service name
cp ${REMOTE}/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
systemctl daemon-reload
systemctl enable simjiang-reminder
systemctl restart simjiang-reminder
sleep 1
systemctl --no-pager -l status simjiang-reminder | head -n 25
echo '---'
# verify ONLY this service / port
curl -sS http://127.0.0.1:8787/api/status || true
echo
curl -sS http://127.0.0.1:8787/api/public-settings || true
echo
echo 'SCOPE_OK only simjiang-reminder'
`
  );
  console.log(r.out);
  if (r.errOut) console.log("stderr:", r.errOut);
  if (r.code !== 0) {
    console.error("deploy command failed code", r.code);
    process.exitCode = 1;
  } else {
    console.log("DEPLOY OK");
  }
  conn.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
