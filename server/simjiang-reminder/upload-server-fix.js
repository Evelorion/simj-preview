const fs = require("fs");
const path = require("path");
const { Client } = require("ssh2");
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
const conn = new Client();
conn.on("ready", () => {
  conn.sftp((err, sftp) => {
    if (err) throw err;
    const lp = path.join(__dirname, "server.py");
    const rp = "/opt/simjiang-reminder/server.py";
    sftp.fastPut(lp, rp, (e) => {
      if (e) throw e;
      console.log("uploaded server.py", fs.statSync(lp).size);
      conn.exec(
        "systemctl restart simjiang-reminder; sleep 1; curl -sS -D- -o /tmp/reg_out.json -X POST http://127.0.0.1:8787/api/account/register -H 'Content-Type: application/json' -d '{\"username\":\"http11_probe\",\"password\":\"testpass12\"}'; head -5 /tmp/reg_out.json; echo; head -20 /tmp/reg_out.json 2>/dev/null; curl -sS -D- -o /dev/null http://127.0.0.1:8787/api/status | head -5; echo DONE",
        (e2, stream) => {
          let o = "";
          stream.on("data", (d) => (o += d));
          stream.stderr.on("data", (d) => (o += d));
          stream.on("close", () => { console.log(o); conn.end(); });
        }
      );
    });
  });
}).connect({ host: HOST, username: USER, password: PASS, readyTimeout: 30000 });
