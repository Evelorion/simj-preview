const { Client } = require("ssh2");
const HOST = process.env.SIMJ_HOST;
const USER = process.env.SIMJ_USER;
const PASS = process.env.SIMJ_PASS;
if (!PASS) {
  console.error("Set SIMJ_PASS in the environment before running this cleanup");
  process.exit(1);
}
if (!HOST) {
  console.error("Set SIMJ_HOST in the environment before running this cleanup");
  process.exit(1);
}
if (!USER) {
  console.error("Set SIMJ_USER in the environment before running this cleanup");
  process.exit(1);
}
const cmd = `
cd /opt/simjiang-reminder/web
echo "=== before ==="
du -sh .
# remove historical leftovers only inside simjiang-reminder/web
rm -f index.html.prev.* index.html.bak.* simj-admin.js.bak.* simj-admin.js.prev.* countries-lite.json* vendor-globe*.js globe-raw.html *.tgz 2>/dev/null || true
# keep current split package
echo "=== after ==="
du -sh . vendor app data assets 2>/dev/null
ls -lh index.html vendor/globe.gl.js app/globe-app.js data/countries.geojson
echo -n "flags: "; ls assets/flags 2>/dev/null | wc -l
echo CLEAN_OK
`;
const conn = new Client();
conn
  .on("ready", () => {
    conn.exec(cmd, (err, stream) => {
      if (err) {
        console.error(err);
        process.exit(1);
      }
      let out = "";
      stream.on("data", (d) => (out += d));
      stream.stderr.on("data", (d) => (out += d));
      stream.on("close", () => {
        console.log(out);
        conn.end();
      });
    });
  })
  .connect({
    host: HOST,
    username: USER,
    password: PASS,
    readyTimeout: 30000,
  });
