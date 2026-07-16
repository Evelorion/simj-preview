const fs = require("fs");
const path = require("path");
const { Client } = require("ssh2");

const REMOTE = "/opt/simjiang-reminder";
const HOST = process.env.SIMJ_HOST;
const USER = process.env.SIMJ_USER || "root";
const PASS = process.env.SIMJ_PASS;
if (!PASS) {
  console.error("Set SIMJ_PASS in the environment before uploading");
  process.exit(1);
}
if (!HOST) {
  console.error("Set SIMJ_HOST in the environment before uploading");
  process.exit(1);
}
const files = [
  ["web/index.html", "web/index.html"],
  ["web/app/globe-app.js", "web/app/globe-app.js"],
  ["web/simj-portal.js", "web/simj-portal.js"],
];

const conn = new Client();
conn
  .on("ready", () => {
    conn.sftp(async (err, sftp) => {
      if (err) throw err;
      for (const [rel, rrel] of files) {
        const lp = path.join(__dirname, rel);
        const rp = `${REMOTE}/${rrel}`;
        await new Promise((res, rej) =>
          sftp.fastPut(lp, rp, (e) => (e ? rej(e) : res()))
        );
        console.log("uploaded", rel, fs.statSync(lp).size);
      }
      conn.exec(
        "systemctl restart simjiang-reminder; sleep 1; curl -sS http://127.0.0.1:8787/ | tr '\\n' ' ' | grep -oE 'fly8|globe-app' | sort -u; curl -sS -o /dev/null -w 'app:%{size_download}\\n' http://127.0.0.1:8787/app/globe-app.js?v=fly8; echo DONE",
        (e, stream) => {
          let o = "";
          stream.on("data", (d) => (o += d));
          stream.stderr.on("data", (d) => (o += d));
          stream.on("close", () => {
            console.log(o);
            conn.end();
          });
        }
      );
    });
  })
  .connect({
    host: HOST,
    username: USER,
    password: PASS,
    readyTimeout: 30000,
  });
