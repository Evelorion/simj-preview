const { Client } = require("ssh2");
const PASS = process.env.SIMJ_PASS;
if (!PASS) {
  console.error("Set SIMJ_PASS in the environment before running this check");
  process.exit(1);
}
if (!process.env.SIMJ_HOST) {
  console.error("Set SIMJ_HOST in the environment before running this check");
  process.exit(1);
}

const cmd = `
set -e
echo "=== status ==="
curl -sS http://127.0.0.1:8787/api/status; echo
echo "=== static ==="
for p in / /vendor/globe.gl.js /app/globe-app.js /data/countries.geojson /data/offline-country-meta.json /data/micro-features.json /assets/transparent.png /assets/outline-border.png /simj-portal.js /assets/flags/CN.png; do
  code=$(curl -sS -o /tmp/simjchk -w '%{http_code}:%{size_download}' "http://127.0.0.1:8787$p" || echo fail)
  echo "$p -> $code"
done
echo "=== index has loader ==="
curl -sS http://127.0.0.1:8787/ | tr '\\n' ' ' | grep -oE 'vendor/globe.gl.js|app/globe-app.js|id="search"|id="borders"|id="outline"|simj-portal' | sort -u
echo "=== app features ==="
curl -sS http://127.0.0.1:8787/app/globe-app.js | tr '\\n' ' ' | grep -oE 'prepareCountries|selectCountry|bindUI|MapServer/tile|setOutlineMode|country-dial|SIMJ_GLOBE|TRANSPARENT_TEXTURE|applyMapMode' | sort -u
echo "=== disk ==="
du -sh /opt/simjiang-reminder/web /opt/simjiang-reminder/web/vendor /opt/simjiang-reminder/web/app /opt/simjiang-reminder/web/data /opt/simjiang-reminder/web/assets 2>/dev/null || true
echo -n "flags count: "; ls /opt/simjiang-reminder/web/assets/flags 2>/dev/null | wc -l
echo SCOPE_OK
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
      stream.on("close", (code) => {
        console.log(out);
        conn.end();
        process.exit(code || 0);
      });
    });
  })
  .on("error", (e) => {
    console.error(e);
    process.exit(1);
  })
  .connect({
    host: process.env.SIMJ_HOST,
    port: 22,
    username: process.env.SIMJ_USER || "root",
    password: PASS,
    readyTimeout: 30000,
  });
