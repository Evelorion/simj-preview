#!/usr/bin/env bash
set -euo pipefail

BASE="${SIMJ_BASE:-/opt/simjiang-reminder}"
DB="${BASE}/data.db"
BACKUP_DIR="${SIMJ_BACKUP_DIR:-${BASE}/backups}"
KEEP="${SIMJ_BACKUP_KEEP:-30}"

mkdir -p "${BACKUP_DIR}"

if [[ ! -f "${DB}" ]]; then
  exit 0
fi

timestamp="$(date +%Y%m%d-%H%M%S)"
target="${BACKUP_DIR}/data-${timestamp}.db"

python3 - "${DB}" "${target}" <<'PY'
import sqlite3
import sys

source_path, target_path = sys.argv[1], sys.argv[2]
source = sqlite3.connect(source_path)
target = sqlite3.connect(target_path)
try:
    source.backup(target)
finally:
    target.close()
    source.close()
PY

find "${BACKUP_DIR}" -type f -name 'data-*.db' | sort -r | tail -n "+$((KEEP + 1))" | xargs -r rm -f
