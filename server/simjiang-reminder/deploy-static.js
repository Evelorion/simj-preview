/**
 * Deploy ONLY simjiang-reminder static web package + server.py
 * Does not touch any other VPS project.
 */
const fs = require("fs");
const path = require("path");
const { Client } = require("ssh2");

const HOST = process.env.SIMJ_HOST;
const USER = process.env.SIMJ_USER || "root";
const PASS = process.env.SIMJ_PASS;
const REMOTE = "/opt/simjiang-reminder";
const LOCAL = __dirname;

function exec(conn, cmd) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, (err, stream) => {
      if (err) return reject(err);
      let out = "",
        errOut = "";
      stream.on("data", (d) => (out += d));
      stream.stderr.on("data", (d) => (errOut += d));
      stream.on("close", (code) => resolve({ code, out, errOut }));
    });
  });
}

function sftpPut(sftp, localPath, remotePath) {
  return new Promise((resolve, reject) => {
    sftp.fastPut(localPath, remotePath, (err) => (err ? reject(err) : resolve()));
  });
}

function sftpMkdir(sftp, remotePath) {
  return new Promise((resolve) => {
    sftp.mkdir(remotePath, () => resolve());
  });
}

async function ensureDir(sftp, remoteDir) {
  const parts = remoteDir.split("/").filter(Boolean);
  let cur = "";
  for (const p of parts) {
    cur += "/" + p;
    await sftpMkdir(sftp, cur);
  }
}

function walkFiles(dir, base = dir, acc = []) {
  for (const name of fs.readdirSync(dir)) {
    if (name.startsWith(".") || name.endsWith(".bak") || name.includes("backup") || name.includes("monolith") || name.includes("globe-raw") || name === "build-split.js" || name.startsWith("_"))
      continue;
    // skip huge leftovers
    if (name === "index.html.monolith-backup" || name === "index.html.bak-pre-online-opt") continue;
    if (name === "vendor-globe.js" || name === "vendor-globe-full.js" || name === "countries-lite.json") continue;
    const p = path.join(dir, name);
    const st = fs.statSync(p);
    if (st.isDirectory()) walkFiles(p, base, acc);
    else acc.push(path.relative(base, p));
  }
  return acc;
}

async function main() {
  if (!PASS) {
    throw new Error("Set SIMJ_PASS in the environment before deploying");
  }
  if (!HOST) {
    throw new Error("Set SIMJ_HOST in the environment before deploying");
  }
  const conn = new Client();
  await new Promise((resolve, reject) => {
    conn
      .on("ready", resolve)
      .on("error", reject)
      .connect({ host: HOST, port: 22, username: USER, password: PASS, readyTimeout: 30000 });
  });
  console.log("SSH connected");

  // only stop/restart simjiang-reminder
  await exec(conn, `mkdir -p ${REMOTE}/web ${REMOTE}/web/vendor ${REMOTE}/web/app ${REMOTE}/web/assets/flags ${REMOTE}/web/data`);

  const sftp = await new Promise((resolve, reject) => {
    conn.sftp((err, s) => (err ? reject(err) : resolve(s)));
  });

  // server files
  for (const f of ["server.py", "requirements.txt", "simjiang-reminder.service", "backup.sh"]) {
    const lp = path.join(LOCAL, f);
    if (!fs.existsSync(lp)) continue;
    console.log("upload", f);
    await sftpPut(sftp, lp, `${REMOTE}/${f}`);
  }

  const webRoot = path.join(LOCAL, "web");
  let files = walkFiles(webRoot);
  // SIMJ_SKIP_HEAVY=1 (default): skip 20MB+ earth textures on first deploy.
  // Online satellite mode does not need them; offline mode can use outline textures.
  // Set SIMJ_SKIP_HEAVY=0 to upload earth.jpg / earth-border.jpg / cloud.png too.
  const skipHeavy = process.env.SIMJ_SKIP_HEAVY !== "0";
  const heavyNames = new Set([
    "assets/earth.jpg",
    "assets/earth-border.jpg",
    "assets/cloud.png",
    "assets/unused-night.jpg",
    "assets/unused-lights.png",
  ]);
  if (skipHeavy) {
    const before = files.length;
    files = files.filter((rel) => !heavyNames.has(rel.replace(/\\/g, "/")));
    console.log(`skip heavy assets: ${before - files.length} files (set SIMJ_SKIP_HEAVY=0 to include)`);
  }
  console.log("web files to upload:", files.length);
  for (const rel of files) {
    const lp = path.join(webRoot, rel);
    const rp = `${REMOTE}/web/${rel.replace(/\\/g, "/")}`;
    await ensureDir(sftp, path.posix.dirname(rp));
    const mb = fs.statSync(lp).size / 1e6;
    if (mb > 0.2) console.log(`upload web/${rel} (${mb.toFixed(2)} MB)`);
    else if (files.indexOf(rel) % 40 === 0) console.log(`... ${rel}`);
    await sftpPut(sftp, lp, rp);
  }

  const r = await exec(
    conn,
    `
set -e
cp ${REMOTE}/simjiang-reminder.service /etc/systemd/system/simjiang-reminder.service
systemctl daemon-reload
systemctl restart simjiang-reminder
sleep 1
systemctl is-active simjiang-reminder
curl -sS http://127.0.0.1:8787/api/status || true
echo
# sizes on server
du -sh ${REMOTE}/web ${REMOTE}/web/vendor ${REMOTE}/web/app ${REMOTE}/web/assets ${REMOTE}/web/data 2>/dev/null || true
ls -la ${REMOTE}/web/index.html ${REMOTE}/web/vendor/globe.gl.js ${REMOTE}/web/app/globe-app.js | awk '{print $5,$9}'
echo SCOPE_OK only simjiang-reminder
`
  );
  console.log(r.out || r.errOut);
  if (r.code !== 0) process.exitCode = 1;
  else console.log("DEPLOY OK");
  conn.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
