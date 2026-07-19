#!/usr/bin/env python3
"""
simJ Cloud — End-to-End Encrypted backend (v6)

Security model
--------------
* Login password: PBKDF2 hash stored server-side (login only).
* Vault crypto (App/Web): AES-256-GCM key derived from ACCOUNT PASSWORD
  (PBKDF2 310000, salt from username). Server NEVER decrypts vault.
* privateKey: random, shown ONCE at register; only SHA-256 stored.
  Used ONLY for password-reset identity — NOT for vault encryption.
* coverage_json: per-account map metadata + optional number card samples
  (returned only to authenticated owner). Used by globe highlight / web cards.

Full developer guide: repo docs/DEVELOPMENT.md
"""
from __future__ import annotations

import hashlib
import hmac
import json
import mimetypes
import os
import re
import secrets
import sqlite3
import threading
import time
import urllib.parse
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

DEFAULT_BASE = Path("/opt/simjiang-reminder")
BASE = Path(os.getenv("SIMJ_BASE") or DEFAULT_BASE)
DB = BASE / "data.db"
WEB_DIR = BASE / "web"
HOST = os.getenv("SIMJ_HOST") or "0.0.0.0"
PORT = int(os.getenv("SIMJ_PORT") or "8787")

USERNAME_RE = re.compile(r"^[A-Za-z0-9_.@-]{3,64}$")
PRIVATE_KEY_RE = re.compile(r"^[A-Za-z0-9_-]{24,80}$")
SESSION_TTL = 60 * 60 * 24 * 7
SCHEMA_VERSION = "6-e2ee-admin2fa"
SERVICE_VERSION = "v6-e2ee-globe-admin2fa"


# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

def clean(s: str) -> str:
    return "".join(str(s or "").strip().split())


def b64url_key(nbytes: int = 32) -> str:
    # urlsafe base64 without padding — matches App b64u(token_bytes)
    import base64

    return base64.urlsafe_b64encode(secrets.token_bytes(nbytes)).decode("ascii").rstrip("=")


def password_hash(password: str, salt: str | None = None):
    import base64

    salt_bytes = base64.urlsafe_b64decode(salt + "=" * (-len(salt) % 4)) if salt else secrets.token_bytes(16)
    digest = hashlib.pbkdf2_hmac("sha256", str(password or "").encode("utf-8"), salt_bytes, 210_000, dklen=32)
    salt_out = base64.urlsafe_b64encode(salt_bytes).decode("ascii").rstrip("=")
    hash_out = base64.urlsafe_b64encode(digest).decode("ascii").rstrip("=")
    return salt_out, hash_out


def verify_password(password: str, salt: str, expected: str) -> bool:
    _, actual = password_hash(password, salt)
    return hmac.compare_digest(actual, expected or "")


def private_key_hash(private_key: str) -> str:
    return hashlib.sha256(clean(private_key).encode("utf-8")).hexdigest()


def private_key_tail(private_key: str) -> str:
    k = clean(private_key)
    return k[-6:] if len(k) >= 6 else k


def _b32_decode(secret: str) -> bytes:
    import base64

    s = re.sub(r"[^A-Z2-7]", "", str(secret or "").upper())
    if not s:
        raise ValueError("empty totp secret")
    pad = "=" * ((8 - len(s) % 8) % 8)
    return base64.b32decode(s + pad, casefold=True)


def totp_code(secret: str, for_time: int | None = None, step: int = 30, digits: int = 6) -> str:
    """RFC 6238 TOTP (SHA-1), no external dependency."""
    import struct

    key = _b32_decode(secret)
    counter = int((for_time if for_time is not None else time.time()) // step)
    msg = struct.pack(">Q", counter)
    digest = hmac.new(key, msg, hashlib.sha1).digest()
    offset = digest[-1] & 0x0F
    num = struct.unpack(">I", digest[offset : offset + 4])[0] & 0x7FFFFFFF
    return str(num % (10**digits)).zfill(digits)


def verify_totp(secret: str, code: str, window: int = 1) -> bool:
    raw = re.sub(r"\s+", "", str(code or ""))
    if not raw.isdigit() or not secret:
        return False
    now = int(time.time())
    for w in range(-window, window + 1):
        if hmac.compare_digest(totp_code(secret, now + w * 30), raw.zfill(6)[-6:]):
            return True
        # also accept without leading zeros normalization
        if hmac.compare_digest(totp_code(secret, now + w * 30), raw):
            return True
    return False


def new_totp_secret() -> str:
    import base64

    # 20 bytes → base32, standard authenticator length
    return base64.b32encode(secrets.token_bytes(20)).decode("ascii").rstrip("=")


def otpauth_uri(secret: str, username: str, issuer: str = "SIMJ Admin") -> str:
    label = urllib.parse.quote(f"{issuer}:{username}")
    q = urllib.parse.urlencode(
        {
            "secret": secret,
            "issuer": issuer,
            "algorithm": "SHA1",
            "digits": "6",
            "period": "30",
        }
    )
    return f"otpauth://totp/{label}?{q}"


# ---------------------------------------------------------------------------
# database
# ---------------------------------------------------------------------------

def db():
    conn = sqlite3.connect(DB, timeout=30)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA foreign_keys=ON")
    return conn


def commit(conn):
    conn.commit()
    try:
        conn.execute("PRAGMA wal_checkpoint(TRUNCATE)")
    except Exception:
        pass


def init_db():
    BASE.mkdir(parents=True, exist_ok=True)
    WEB_DIR.mkdir(parents=True, exist_ok=True)
    conn = db()
    conn.executescript(
        """
        CREATE TABLE IF NOT EXISTS accounts (
            id            TEXT PRIMARY KEY,
            username      TEXT NOT NULL UNIQUE,
            role          TEXT NOT NULL DEFAULT 'user',
            password_salt TEXT NOT NULL,
            password_hash TEXT NOT NULL,
            private_key_hash TEXT NOT NULL,
            private_key_tail TEXT NOT NULL DEFAULT '',
            enabled       INTEGER NOT NULL DEFAULT 1,
            created_at    INTEGER NOT NULL,
            updated_at    INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS sessions (
            token      TEXT PRIMARY KEY,
            account_id TEXT NOT NULL,
            username   TEXT NOT NULL,
            role       TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            expires_at INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS encrypted_sync (
            account_id    TEXT PRIMARY KEY,
            envelope      TEXT NOT NULL,
            coverage_json TEXT NOT NULL DEFAULT '{}',
            records_count INTEGER NOT NULL DEFAULT 0,
            updated_at    INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS sync_backups (
            id            INTEGER PRIMARY KEY AUTOINCREMENT,
            account_id    TEXT NOT NULL,
            envelope      TEXT NOT NULL,
            coverage_json TEXT NOT NULL DEFAULT '{}',
            records_count INTEGER NOT NULL DEFAULT 0,
            reason        TEXT NOT NULL DEFAULT '',
            created_at    INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS server_settings (
            key        TEXT PRIMARY KEY,
            value      TEXT NOT NULL,
            updated_at INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS schema_meta (
            key   TEXT PRIMARY KEY,
            value TEXT NOT NULL
        );
        """
    )
    now = int(time.time())
    conn.execute(
        "INSERT OR IGNORE INTO server_settings(key,value,updated_at) VALUES(?,?,?)",
        ("allow_registration", "1", now),
    )
    # Admin second factors: OFF by default — admin must enable in dashboard
    for k, v in (
        ("admin_2fa_enabled", "0"),
        ("admin_2fa_secret", ""),
        ("admin_2fa_pending_secret", ""),
        ("admin_keepass_enabled", "0"),
        ("admin_keepass_salt", ""),
        ("admin_keepass_hash", ""),
    ):
        conn.execute(
            "INSERT OR IGNORE INTO server_settings(key,value,updated_at) VALUES(?,?,?)",
            (k, v, now),
        )
    # Legacy extra admin factor was removed; clear old state on startup.
    for k, v in (
        ("admin_keepass_enabled", "0"),
        ("admin_keepass_salt", ""),
        ("admin_keepass_hash", ""),
    ):
        conn.execute(
            "INSERT OR REPLACE INTO server_settings(key,value,updated_at) VALUES(?,?,?)",
            (k, v, now),
        )
    conn.execute(
        "INSERT OR REPLACE INTO schema_meta(key,value) VALUES(?,?)",
        ("version", SCHEMA_VERSION),
    )
    sanitize_stored_coverage(conn)
    commit(conn)
    conn.close()


def get_setting(conn, key: str, default: str = "") -> str:
    row = conn.execute("SELECT value FROM server_settings WHERE key=?", (key,)).fetchone()
    return row["value"] if row else default


def set_setting(conn, key: str, value: str):
    conn.execute(
        "INSERT OR REPLACE INTO server_settings(key,value,updated_at) VALUES(?,?,?)",
        (key, str(value), int(time.time())),
    )


def allow_registration(conn) -> bool:
    # first account always allowed
    n = int(conn.execute("SELECT count(*) c FROM accounts").fetchone()["c"] or 0)
    if n <= 0:
        return True
    return get_setting(conn, "allow_registration", "1") != "0"


def admin_security_public(conn) -> dict:
    """Safe flags for login UI (no secrets)."""
    return {
        "admin2faEnabled": get_setting(conn, "admin_2fa_enabled", "0") == "1"
        and bool(get_setting(conn, "admin_2fa_secret", "")),
        "adminKeepassEnabled": False,
    }


def admin_security_detail(conn) -> dict:
    """Admin dashboard view — still no raw secrets except pending setup URI."""
    flags = admin_security_public(conn)
    pending = get_setting(conn, "admin_2fa_pending_secret", "")
    return {
        **flags,
        "admin2faConfigured": bool(get_setting(conn, "admin_2fa_secret", "")),
        "adminKeepassConfigured": False,
        "admin2faPending": bool(pending),
    }


def public_settings(conn=None) -> dict:
    own = conn is None
    if own:
        conn = db()
    try:
        n = int(conn.execute("SELECT count(*) c FROM accounts").fetchone()["c"] or 0)
        return {
            "allowRegistration": allow_registration(conn),
            "users": n,
            "service": "simjiang-reminder",
            "version": SERVICE_VERSION,
            "e2ee": True,
            **admin_security_public(conn),
        }
    finally:
        if own:
            conn.close()


# ---------------------------------------------------------------------------
# accounts / sessions
# ---------------------------------------------------------------------------

def register_account(username: str, password: str):
    username = str(username or "").strip()
    password = str(password or "")
    if not USERNAME_RE.match(username):
        return False, "用户名需要 3-64 位，可用字母、数字、点、下划线、@ 或 -", None
    if len(password) < 8:
        return False, "密码至少 8 位", None

    conn = db()
    try:
        if not allow_registration(conn):
            return False, "服务器已关闭新用户注册", None
        if conn.execute("SELECT 1 FROM accounts WHERE username=?", (username,)).fetchone():
            return False, "用户名已存在", None

        existing = int(conn.execute("SELECT count(*) c FROM accounts").fetchone()["c"] or 0)
        role = "owner" if existing <= 0 else "user"
        account_id = str(uuid.uuid4())
        private_key = b64url_key(32)
        salt, pwd = password_hash(password)
        now = int(time.time())
        conn.execute(
            """INSERT INTO accounts(
                id, username, role, password_salt, password_hash,
                private_key_hash, private_key_tail, enabled, created_at, updated_at
            ) VALUES(?,?,?,?,?,?,?,?,?,?)""",
            (
                account_id,
                username,
                role,
                salt,
                pwd,
                private_key_hash(private_key),
                private_key_tail(private_key),
                1,
                now,
                now,
            ),
        )
        commit(conn)
        token = create_session(conn, account_id, username, role)
        return True, "注册成功", {
            "token": token,
            "privateKey": private_key,
            "username": username,
            "role": role,
            "accountId": account_id,
            "privateKeyTail": private_key_tail(private_key),
        }
    finally:
        conn.close()


def login_account(username: str, password: str):
    username = str(username or "").strip()
    conn = db()
    try:
        row = conn.execute(
            "SELECT id, username, role, password_salt, password_hash, enabled, private_key_tail FROM accounts WHERE username=?",
            (username,),
        ).fetchone()
        if not row or not int(row["enabled"]):
            return False, "用户名或密码错误", None
        if not verify_password(password, row["password_salt"], row["password_hash"]):
            return False, "用户名或密码错误", None
        token = create_session(conn, row["id"], row["username"], row["role"])
        return True, "登录成功", {
            "token": token,
            "username": row["username"],
            "role": row["role"],
            "accountId": row["id"],
            "privateKeyTail": row["private_key_tail"] or "",
            # private key is NEVER returned on login
        }
    finally:
        conn.close()


def login_admin(username: str, password: str, totp: str = "", keepass: str = ""):
    """
    Admin portal login.
    Password is always required. 2FA is enforced only when enabled.
    The legacy extra-factor argument is ignored for compatibility.
    """
    username = str(username or "").strip()
    conn = db()
    try:
        row = conn.execute(
            "SELECT id, username, role, password_salt, password_hash, enabled FROM accounts WHERE username=?",
            (username,),
        ).fetchone()
        if not row or not int(row["enabled"]):
            return False, "用户名或密码错误", None
        if not verify_password(password, row["password_salt"], row["password_hash"]):
            return False, "用户名或密码错误", None
        if not is_admin(row["role"]):
            return False, "该账户不是管理员", None

        sec = admin_security_public(conn)
        need_2fa = sec["admin2faEnabled"]

        if need_2fa:
            secret = get_setting(conn, "admin_2fa_secret", "")
            if not secret:
                return False, "管理员 2FA 已开启但未配置密钥，请先在后台完成设置", None
            if not str(totp or "").strip():
                return False, "请输入 2FA 验证码", {
                    "need2fa": True,
                    "needKeepass": False,
                    "step": "second_factor",
                }
            if not verify_totp(secret, totp):
                return False, "2FA 验证码错误", {
                    "need2fa": True,
                    "needKeepass": False,
                    "step": "second_factor",
                }

        token = create_session(conn, row["id"], row["username"], row["role"])
        return True, "管理员登录成功", {
            "token": token,
            "username": row["username"],
            "role": row["role"],
            "accountId": row["id"],
            "need2fa": False,
            "needKeepass": False,
        }
    finally:
        conn.close()


def admin_set_password(account_id: str, old_password: str, new_password: str):
    new_password = str(new_password or "")
    if len(new_password) < 8:
        return False, "新密码至少 8 位"
    conn = db()
    try:
        row = conn.execute(
            "SELECT password_salt, password_hash, role FROM accounts WHERE id=?",
            (account_id,),
        ).fetchone()
        if not row or not is_admin(row["role"]):
            return False, "需要管理员权限"
        if not verify_password(old_password, row["password_salt"], row["password_hash"]):
            return False, "当前密码错误"
        salt, pwd = password_hash(new_password)
        conn.execute(
            "UPDATE accounts SET password_salt=?, password_hash=?, updated_at=? WHERE id=?",
            (salt, pwd, int(time.time()), account_id),
        )
        commit(conn)
        return True, "管理员密码已更新"
    finally:
        conn.close()


def active_admin_count(conn, exclude_id: str = "") -> int:
    row = conn.execute(
        """SELECT count(*) c FROM accounts
           WHERE role IN ('owner','admin') AND enabled=1 AND id<>?""",
        (exclude_id or "",),
    ).fetchone()
    return int((row["c"] if row else 0) or 0)


def admin_manage_account(admin_id: str, payload: dict):
    action = str(payload.get("action") or "").strip().lower()
    account_id = str(payload.get("accountId") or payload.get("id") or "").strip()
    if not account_id:
        return 400, {"ok": False, "message": "缺少账户 ID"}

    conn = db()
    try:
        row = conn.execute(
            "SELECT id, username, role, enabled FROM accounts WHERE id=?",
            (account_id,),
        ).fetchone()
        if not row:
            return 404, {"ok": False, "message": "账户不存在"}

        now = int(time.time())
        if action in ("rename", "set_username", "username"):
            username = str(payload.get("username") or payload.get("newUsername") or "").strip()
            if not USERNAME_RE.match(username):
                return 400, {"ok": False, "message": "用户名需为 3-64 位，可用字母、数字、点、下划线、@ 或 -"}
            exists = conn.execute(
                "SELECT 1 FROM accounts WHERE username=? AND id<>?",
                (username, account_id),
            ).fetchone()
            if exists:
                return 400, {"ok": False, "message": "用户名已存在"}
            conn.execute(
                "UPDATE accounts SET username=?, updated_at=? WHERE id=?",
                (username, now, account_id),
            )
            conn.execute("UPDATE sessions SET username=? WHERE account_id=?", (username, account_id))
            commit(conn)
            return 200, {
                "ok": True,
                "message": "用户名已修改；服务端不会解密或重写该账户的加密包",
                "account": {"id": account_id, "username": username},
            }

        if action in ("set_enabled", "enable", "disable"):
            if action == "enable":
                enabled = True
            elif action == "disable":
                enabled = False
            else:
                raw = payload.get("enabled")
                enabled = str(raw).strip().lower() in ("1", "true", "yes", "on") if isinstance(raw, str) else bool(raw)
            if account_id == admin_id and not enabled:
                return 400, {"ok": False, "message": "不能停用当前登录的管理员账户"}
            if not enabled and is_admin(row["role"]) and active_admin_count(conn, account_id) <= 0:
                return 400, {"ok": False, "message": "不能停用最后一个可用管理员账户"}
            conn.execute(
                "UPDATE accounts SET enabled=?, updated_at=? WHERE id=?",
                (1 if enabled else 0, now, account_id),
            )
            if not enabled:
                conn.execute("DELETE FROM sessions WHERE account_id=?", (account_id,))
            commit(conn)
            return 200, {
                "ok": True,
                "message": "账户已启用" if enabled else "账户已停用并踢出登录",
                "account": {"id": account_id, "enabled": enabled},
            }

        if action in ("delete", "remove"):
            if account_id == admin_id:
                return 400, {"ok": False, "message": "不能删除当前登录的管理员账户"}
            if is_admin(row["role"]) and active_admin_count(conn, account_id) <= 0:
                return 400, {"ok": False, "message": "不能删除最后一个可用管理员账户"}
            username = row["username"]
            conn.execute("DELETE FROM sessions WHERE account_id=?", (account_id,))
            conn.execute("DELETE FROM sync_backups WHERE account_id=?", (account_id,))
            conn.execute("DELETE FROM encrypted_sync WHERE account_id=?", (account_id,))
            conn.execute("DELETE FROM accounts WHERE id=?", (account_id,))
            commit(conn)
            return 200, {"ok": True, "message": f"账户 {username} 已删除，云端加密包和备份已清理"}

        return 400, {"ok": False, "message": "未知账户操作"}
    finally:
        conn.close()


def reset_password(username: str, private_key: str, new_password: str):
    username = str(username or "").strip()
    private_key = clean(private_key)
    new_password = str(new_password or "")
    if not USERNAME_RE.match(username):
        return False, "用户名无效"
    if not PRIVATE_KEY_RE.match(private_key):
        return False, "私钥格式不正确"
    if len(new_password) < 8:
        return False, "新密码至少 8 位"

    conn = db()
    try:
        row = conn.execute(
            "SELECT id, private_key_hash, enabled FROM accounts WHERE username=?",
            (username,),
        ).fetchone()
        if not row or not int(row["enabled"]):
            return False, "用户不存在或已禁用"
        if not hmac.compare_digest(row["private_key_hash"], private_key_hash(private_key)):
            return False, "私钥不正确，无法重置密码"
        salt, pwd = password_hash(new_password)
        now = int(time.time())
        conn.execute(
            "UPDATE accounts SET password_salt=?, password_hash=?, updated_at=? WHERE id=?",
            (salt, pwd, now, row["id"]),
        )
        # invalidate all sessions
        conn.execute("DELETE FROM sessions WHERE account_id=?", (row["id"],))
        commit(conn)
        return True, "密码已重置，请用新密码登录"
    finally:
        conn.close()


def create_session(conn, account_id: str, username: str, role: str) -> str:
    token = secrets.token_urlsafe(32)
    now = int(time.time())
    conn.execute("DELETE FROM sessions WHERE expires_at<?", (now,))
    conn.execute(
        "INSERT INTO sessions(token, account_id, username, role, created_at, expires_at) VALUES(?,?,?,?,?,?)",
        (token, account_id, username, role, now, now + SESSION_TTL),
    )
    commit(conn)
    return token


def parse_cookies(headers) -> dict:
    out = {}
    raw = headers.get("Cookie", "") or ""
    for part in raw.split(";"):
        if "=" in part:
            k, v = part.split("=", 1)
            out[k.strip()] = urllib.parse.unquote(v.strip())
    return out


def session_from_headers(headers):
    token = parse_cookies(headers).get("simj_session", "")
    auth = headers.get("Authorization", "") or ""
    if not token and auth.lower().startswith("bearer "):
        token = auth.split(" ", 1)[1].strip()
    # also accept X-Session-Token
    if not token:
        token = headers.get("X-Session-Token", "") or ""
    if not token:
        return None
    now = int(time.time())
    conn = db()
    try:
        row = conn.execute(
            """SELECT s.token, s.account_id, s.username, s.role, s.expires_at, a.enabled
               FROM sessions s JOIN accounts a ON a.id=s.account_id
               WHERE s.token=?""",
            (token,),
        ).fetchone()
        if not row or int(row["expires_at"]) < now or not int(row["enabled"]):
            if row:
                conn.execute("DELETE FROM sessions WHERE token=?", (token,))
                commit(conn)
            return None
        return {
            "token": row["token"],
            "account_id": row["account_id"],
            "username": row["username"],
            "role": row["role"] or "user",
        }
    finally:
        conn.close()


def is_admin(role: str) -> bool:
    return role in ("owner", "admin")


def load_coverage(conn, account_id: str) -> dict:
    row = conn.execute(
        "SELECT coverage_json, records_count, updated_at FROM encrypted_sync WHERE account_id=?",
        (account_id,),
    ).fetchone()
    if not row:
        return {"countries": [], "countryCount": 0, "records": 0, "esims": 0, "updatedAt": 0}
    try:
        cov = json.loads(row["coverage_json"] or "{}")
    except Exception:
        cov = {}
    if not isinstance(cov, dict):
        cov = {}
    cov = normalize_coverage(cov)
    cov.setdefault("countries", [])
    cov.setdefault("countryCount", len(cov.get("countries") or []))
    cov.setdefault("records", int(row["records_count"] or 0))
    cov.setdefault("esims", int(cov.get("esims") or 0))
    cov["updatedAt"] = int(row["updated_at"] or 0)
    return cov


def normalize_coverage(raw) -> dict:
    if not isinstance(raw, dict):
        return {"countries": [], "countryCount": 0, "records": 0, "esims": 0}
    countries = []
    for item in raw.get("countries") or []:
        if not isinstance(item, dict):
            continue
        iso = str(item.get("iso") or item.get("iso2") or item.get("ISO_A2") or "").strip().upper()
        if len(iso) != 2:
            continue
        esims = int(item.get("esims") or 0)
        records = int(item.get("records") or 0)
        # Account-private metadata only. Full numbers must stay inside encryptedVault.
        samples_out = []
        for s in (item.get("samples") or [])[:120]:
            if not isinstance(s, dict):
                continue
            raw_number = str(s.get("number") or s.get("num") or "").strip()[:40]
            digits = re.sub(r"\D", "", raw_number)
            last4 = re.sub(r"\D", "", str(s.get("last4") or ""))[-4:] or digits[-4:]
            mask = str(s.get("mask") or "").strip()[:48]
            mask_digits = re.sub(r"\D", "", mask)
            if raw_number and (not mask or (digits and digits in mask_digits)):
                mask = ("**** " + (last4 or "????")).strip()
            elif not mask:
                mask = ("**** " + (last4 or "????")).strip()
            if not last4 and not mask:
                continue
            samples_out.append(
                {
                    "id": str(s.get("id") or "")[:64],
                    "last4": last4 or "????",
                    "mask": mask,
                    "op": str(s.get("op") or "")[:40],
                    "esim": bool(s.get("esim")),
                    "code": str(s.get("code") or "")[:12],
                    "name": str(s.get("name") or "")[:40],
                    "flag": str(s.get("flag") or "")[:8],
                    "expire": str(s.get("expire") or s.get("expireDate") or "")[:32],
                    "balance": str(s.get("balance") or "")[:32],
                    "cardType": str(s.get("cardType") or "")[:24],
                    "signal": str(s.get("signal") or "")[:24],
                    "note": str(s.get("note") or "")[:80],
                }
            )
        countries.append(
            {
                "iso": iso,
                "name": str(item.get("name") or iso)[:80],
                "records": records,
                "esims": esims,
                "samples": samples_out,
            }
        )
    # only keep countries that have at least one card; highlight prefers esims>0
    countries.sort(key=lambda x: (-x["esims"], -x["records"], x["iso"]))
    return {
        "countries": countries,
        "countryCount": len(countries),
        "records": sum(c["records"] for c in countries) or int(raw.get("records") or 0),
        "esims": sum(c["esims"] for c in countries) or int(raw.get("esims") or 0),
        "updatedAt": int(raw.get("updatedAt") or time.time()),
    }


def sanitize_stored_coverage(conn):
    """Remove plaintext number samples left by older coverage writers."""
    for table, key_col in (("encrypted_sync", "account_id"), ("sync_backups", "id")):
        rows = conn.execute(f"SELECT {key_col}, coverage_json FROM {table}").fetchall()
        for row in rows:
            try:
                raw = json.loads(row["coverage_json"] or "{}")
            except Exception:
                raw = {}
            clean = normalize_coverage(raw)
            clean_text = json.dumps(clean, ensure_ascii=False)
            if clean_text != (row["coverage_json"] or "{}"):
                conn.execute(
                    f"UPDATE {table} SET coverage_json=? WHERE {key_col}=?",
                    (clean_text, row[key_col]),
                )


def records_from_coverage(coverage: dict) -> list[dict]:
    """Build legacy app records from same-account coverage card samples."""
    if not isinstance(coverage, dict):
        return []
    today = time.strftime("%Y-%m-%d")
    out: list[dict] = []
    for country in coverage.get("countries") or []:
        if not isinstance(country, dict):
            continue
        country_name = str(country.get("name") or "")[:80]
        for sample in (country.get("samples") or [])[:120]:
            if not isinstance(sample, dict):
                continue
            number = str(sample.get("number") or sample.get("num") or "").strip()[:40]
            if not number:
                continue
            is_esim = bool(sample.get("esim"))
            out.append(
                {
                    "id": str(sample.get("id") or uuid.uuid4())[:64],
                    "countryCode": str(sample.get("code") or "")[:12] or "+",
                    "countryName": str(sample.get("name") or country_name)[:40],
                    "flag": str(sample.get("flag") or "")[:8],
                    "number": number,
                    "operator": str(sample.get("op") or sample.get("operator") or "")[:40],
                    "expireDate": str(sample.get("expire") or sample.get("expireDate") or "")[:32],
                    "note": str(sample.get("note") or "")[:80],
                    "balance": str(sample.get("balance") or "")[:32],
                    "startDate": str(sample.get("startDate") or today)[:32],
                    "createdAt": str(sample.get("createdAt") or today)[:32],
                    "signalStatus": str(sample.get("signal") or "在线")[:24],
                    "cardType": str(sample.get("cardType") or ("esim" if is_esim else "prepaid"))[:24],
                }
            )
    return out


def purge_backups(conn, account_id: str, keep: int = 50):
    rows = conn.execute(
        "SELECT id FROM sync_backups WHERE account_id=? ORDER BY created_at DESC, id DESC",
        (account_id,),
    ).fetchall()
    if len(rows) <= keep:
        return
    ids = [r["id"] for r in rows[keep:]]
    conn.execute(
        "DELETE FROM sync_backups WHERE account_id=? AND id IN (%s)" % ",".join("?" * len(ids)),
        [account_id] + ids,
    )


# ---------------------------------------------------------------------------
# HTTP
# ---------------------------------------------------------------------------

class H(BaseHTTPRequestHandler):
    server_version = "simJ-E2EE/6"
    # HTTP/1.0 + abrupt close confuses Android HttpURLConnection
    # ("ProtocolException: unexpected end of stream" on register/login).
    protocol_version = "HTTP/1.1"

    def _json(self, code: int, obj: dict, extra_headers: dict | None = None):
        body = json.dumps(obj, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Connection", "close")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", self.headers.get("Origin") or "*")
        self.send_header("Access-Control-Allow-Credentials", "true")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token, X-API-Key")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        if extra_headers:
            for k, v in extra_headers.items():
                self.send_header(k, v)
        self.end_headers()
        self.wfile.write(body)
        try:
            self.wfile.flush()
        except Exception:
            pass

    def _session_cookie(self, token: str) -> dict:
        return {
            "Set-Cookie": "simj_session=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d"
            % (urllib.parse.quote(token), SESSION_TTL)
        }

    def _clear_cookie(self) -> dict:
        return {"Set-Cookie": "simj_session=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0"}

    def _read_json(self):
        try:
            n = int(self.headers.get("Content-Length") or 0)
        except Exception:
            n = 0
        raw = self.rfile.read(n) if n > 0 else b"{}"
        try:
            return json.loads(raw.decode("utf-8") or "{}")
        except Exception:
            return None

    def _session(self):
        return session_from_headers(self.headers)

    def _path(self) -> str:
        return urllib.parse.urlparse(self.path).path

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", self.headers.get("Origin") or "*")
        self.send_header("Access-Control-Allow-Credentials", "true")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token, X-API-Key")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.end_headers()

    # ---- GET ----
    def do_GET(self):
        path = self._path()

        if path == "/api/status":
            conn = db()
            try:
                users = conn.execute("SELECT count(*) c FROM accounts WHERE enabled=1").fetchone()["c"]
                records = conn.execute("SELECT coalesce(sum(records_count),0) c FROM encrypted_sync").fetchone()["c"]
                schema = conn.execute("SELECT value FROM schema_meta WHERE key='version'").fetchone()
                return self._json(
                    200,
                    {
                        "ok": True,
                        "service": "simjiang-reminder",
                        "version": SERVICE_VERSION,
                        "users": users,
                        "records": records,
                        "schemaVersion": schema["value"] if schema else SCHEMA_VERSION,
                        "time": int(time.time()),
                        "e2ee": True,
                        "features": {
                            "account_e2ee": True,
                            "private_key_once": True,
                            "password_reset_by_key": True,
                            "registration_toggle": True,
                            "globe_coverage": True,
                            "admin_portal": True,
                            "user_portal": True,
                            "admin_2fa": True,
                            "admin_keepass": False,
                        },
                    },
                )
            finally:
                conn.close()

        if path == "/api/public-settings":
            return self._json(200, {"ok": True, **public_settings()})

        if path == "/api/account/me":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            conn = db()
            try:
                row = conn.execute(
                    "SELECT private_key_tail, role, created_at FROM accounts WHERE id=?",
                    (sess["account_id"],),
                ).fetchone()
                cov = load_coverage(conn, sess["account_id"])
                sync_row = conn.execute(
                    "SELECT updated_at, records_count FROM encrypted_sync WHERE account_id=?",
                    (sess["account_id"],),
                ).fetchone()
                updated_at = int(sync_row["updated_at"] if sync_row else 0)
                records = int(
                    (sync_row["records_count"] if sync_row else 0) or cov.get("records") or 0
                )
                return self._json(
                    200,
                    {
                        "ok": True,
                        "accountId": sess["account_id"],
                        "username": sess["username"],
                        "role": sess["role"],
                        "canAdmin": is_admin(sess["role"]),
                        "privateKeyTail": (row["private_key_tail"] if row else ""),
                        "apiKeyTail": (row["private_key_tail"] if row else ""),
                        "hasData": bool(records or cov.get("countries")),
                        "hasSettings": bool(records or cov.get("countries")),
                        "records": records,
                        "updatedAt": updated_at,
                        "coverage": cov,
                        "serverSettings": public_settings(conn),
                    },
                )
            finally:
                conn.close()

        # Legacy App endpoints (same account session / e2ee model)
        if path in ("/api/key-info", "/api/meta"):
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            conn = db()
            try:
                row = conn.execute(
                    "SELECT private_key_tail FROM accounts WHERE id=?",
                    (sess["account_id"],),
                ).fetchone()
                sync_row = conn.execute(
                    "SELECT updated_at, records_count FROM encrypted_sync WHERE account_id=?",
                    (sess["account_id"],),
                ).fetchone()
                return self._json(
                    200,
                    {
                        "ok": True,
                        "username": sess["username"],
                        "apiKeyTail": (row["private_key_tail"] if row else ""),
                        "privateKeyTail": (row["private_key_tail"] if row else ""),
                        "records": int(sync_row["records_count"] if sync_row else 0),
                        "updatedAt": int(sync_row["updated_at"] if sync_row else 0),
                        "hasSettings": bool(sync_row),
                        "hasData": bool(sync_row),
                        "mode": "account-e2ee",
                    },
                )
            finally:
                conn.close()

        if path == "/api/reminder-status":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            # Server cannot decrypt vault — reminders run on device.
            return self._json(
                200,
                {
                    "ok": True,
                    "lastCheckAt": 0,
                    "nextCheckAt": 0,
                    "dueNow": 0,
                    "mode": "client-e2ee",
                    "message": "端到端加密：到期检查由 App 本机执行",
                    "lastStats": {"due": 0, "mail": 0, "tg": 0, "duplicate": 0},
                },
            )

        if path == "/api/backups" or path.startswith("/api/backups/"):
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            conn = db()
            try:
                qs = urllib.parse.urlparse(self.path).query
                params = urllib.parse.parse_qs(qs)
                if path == "/api/backups":
                    limit = max(1, min(100, int((params.get("limit") or ["20"])[0] or 20)))
                    total = int(
                        conn.execute(
                            "SELECT count(*) c FROM sync_backups WHERE account_id=?",
                            (sess["account_id"],),
                        ).fetchone()["c"]
                        or 0
                    )
                    rows = conn.execute(
                        """SELECT id, records_count, reason, created_at
                           FROM sync_backups WHERE account_id=?
                           ORDER BY created_at DESC, id DESC LIMIT ?""",
                        (sess["account_id"], limit),
                    ).fetchall()
                    backups = [
                        {
                            "id": r["id"],
                            "records_count": int(r["records_count"] or 0),
                            "reason": r["reason"] or "pre-sync",
                            "created_at": int(r["created_at"] or 0),
                        }
                        for r in rows
                    ]
                    return self._json(
                        200, {"ok": True, "total": total, "backups": backups, "limit": limit}
                    )

                # /api/backups/{id}
                bid_s = path[len("/api/backups/") :].strip("/")
                if not bid_s.isdigit():
                    return self._json(404, {"ok": False, "message": "备份不存在"})
                bid = int(bid_s)
                row = conn.execute(
                    """SELECT id, records_count, reason, created_at, coverage_json
                       FROM sync_backups WHERE account_id=? AND id=?""",
                    (sess["account_id"], bid),
                ).fetchone()
                if not row:
                    return self._json(404, {"ok": False, "message": "备份不存在"})
                try:
                    cov = json.loads(row["coverage_json"] or "{}")
                except Exception:
                    cov = {}
                return self._json(
                    200,
                    {
                        "ok": True,
                        "backup": {
                            "id": row["id"],
                            "records_count": int(row["records_count"] or 0),
                            "reason": row["reason"] or "pre-sync",
                            "created_at": int(row["created_at"] or 0),
                        },
                        "summary": {
                            "records": int(row["records_count"] or 0),
                            "countryCount": int(cov.get("countryCount") or len(cov.get("countries") or [])),
                            "esims": int(cov.get("esims") or 0),
                            "mode": "account-e2ee",
                            "note": "备份仅含加密包与覆盖统计，服务器无法展示号码明文",
                        },
                    },
                )
            finally:
                conn.close()

        if path == "/api/account/coverage":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            conn = db()
            try:
                return self._json(200, {"ok": True, "coverage": load_coverage(conn, sess["account_id"])})
            finally:
                conn.close()

        if path in ("/api/sync", "/api/pull"):
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录", "error": "not logged in"})
            conn = db()
            try:
                row = conn.execute(
                    "SELECT envelope, coverage_json, records_count, updated_at FROM encrypted_sync WHERE account_id=?",
                    (sess["account_id"],),
                ).fetchone()
                if not row:
                    return self._json(404, {"ok": False, "error": "no cloud data", "message": "当前账户暂无云端数据"})
                try:
                    envelope = json.loads(row["envelope"] or "{}")
                except Exception:
                    envelope = {}
                try:
                    coverage = json.loads(row["coverage_json"] or "{}")
                except Exception:
                    coverage = {}
                coverage = normalize_coverage(coverage)
                legacy_records = records_from_coverage(coverage)
                return self._json(
                    200,
                    {
                        "ok": True,
                        "encryptedVault": envelope,
                        "coverage": coverage,
                        # Compatibility for older app builds that only understand
                        # plaintext payload.records. This is still account-private:
                        # /api/sync requires the logged-in user's session token.
                        "payload": {"records": legacy_records},
                        "legacyRecords": len(legacy_records),
                        "records": int(row["records_count"] or 0),
                        "updatedAt": int(row["updated_at"] or 0),
                        "mode": "account-e2ee",
                    },
                )
            finally:
                conn.close()

        if path == "/api/admin/settings":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            if not is_admin(sess["role"]):
                return self._json(403, {"ok": False, "message": "需要管理员权限"})
            conn = db()
            try:
                return self._json(
                    200,
                    {
                        "ok": True,
                        **public_settings(conn),
                        **admin_security_detail(conn),
                    },
                )
            finally:
                conn.close()

        if path == "/api/admin/security":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            if not is_admin(sess["role"]):
                return self._json(403, {"ok": False, "message": "需要管理员权限"})
            conn = db()
            try:
                return self._json(200, {"ok": True, **admin_security_detail(conn)})
            finally:
                conn.close()

        if path == "/api/admin/login-flags":
            # public: login form needs to know which second factors to show
            conn = db()
            try:
                return self._json(200, {"ok": True, **admin_security_public(conn)})
            finally:
                conn.close()

        if path == "/api/admin/users":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            if not is_admin(sess["role"]):
                return self._json(403, {"ok": False, "message": "需要管理员权限"})
            conn = db()
            try:
                rows = conn.execute(
                    """SELECT a.id, a.username, a.role, a.enabled, a.created_at, a.updated_at,
                              coalesce(e.records_count,0) records,
                              e.updated_at sync_updated_at
                       FROM accounts a
                       LEFT JOIN encrypted_sync e ON e.account_id=a.id
                       ORDER BY a.created_at ASC"""
                ).fetchall()
                users = []
                for r in rows:
                    users.append(
                        {
                            "id": r["id"],
                            "username": r["username"],
                            "role": r["role"],
                            "enabled": bool(r["enabled"]),
                            "createdAt": r["created_at"],
                            "updatedAt": r["updated_at"],
                            "records": int(r["records"] or 0),
                            "syncUpdatedAt": int(r["sync_updated_at"] or 0),
                        }
                    )
                # public_settings also has a "users" count — put list last and
                # expose count as userCount so the admin table is not overwritten.
                ps = public_settings(conn)
                return self._json(
                    200,
                    {
                        "ok": True,
                        "allowRegistration": ps.get("allowRegistration"),
                        "userCount": ps.get("users", len(users)),
                        "service": ps.get("service"),
                        "version": ps.get("version"),
                        "e2ee": ps.get("e2ee"),
                        "users": users,
                    },
                )
            finally:
                conn.close()

        if path.startswith("/api/"):
            return self._json(404, {"ok": False, "error": "not found"})

        return self._serve_static()

    # ---- POST ----
    def do_POST(self):
        path = self._path()
        payload = self._read_json()
        if payload is None:
            return self._json(400, {"ok": False, "message": "JSON 无效"})

        if path == "/api/account/register":
            ok, msg, data = register_account(payload.get("username"), payload.get("password"))
            if not ok:
                return self._json(400, {"ok": False, "message": msg})
            headers = self._session_cookie(data["token"])
            return self._json(
                200,
                {
                    "ok": True,
                    "message": msg,
                    "token": data["token"],
                    "privateKey": data["privateKey"],  # shown once — server does not keep plaintext
                    "privateKeyTail": data["privateKeyTail"],
                    "user": {
                        "username": data["username"],
                        "role": data["role"],
                        "accountId": data["accountId"],
                    },
                    "warning": "请立即保存私钥！私钥仅用于忘记密码时重置；日常登录与云端恢复只需账号+密码。服务器不保存私钥。",
                },
                headers,
            )

        if path == "/api/account/login":
            ok, msg, data = login_account(payload.get("username"), payload.get("password"))
            if not ok:
                return self._json(401, {"ok": False, "message": msg})
            return self._json(
                200,
                {
                    "ok": True,
                    "message": msg,
                    "token": data["token"],
                    "user": {
                        "username": data["username"],
                        "role": data["role"],
                        "accountId": data["accountId"],
                        "privateKeyTail": data.get("privateKeyTail") or "",
                    },
                },
                self._session_cookie(data["token"]),
            )

        if path == "/api/account/reset-password":
            ok, msg = reset_password(
                payload.get("username"),
                payload.get("privateKey") or payload.get("private_key") or payload.get("key"),
                payload.get("newPassword") or payload.get("password"),
            )
            return self._json(200 if ok else 400, {"ok": ok, "message": msg})

        if path == "/api/account/logout":
            sess = self._session()
            if sess:
                conn = db()
                try:
                    conn.execute("DELETE FROM sessions WHERE token=?", (sess["token"],))
                    commit(conn)
                finally:
                    conn.close()
            return self._json(200, {"ok": True}, self._clear_cookie())

        if path == "/api/admin/settings":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            if not is_admin(sess["role"]):
                return self._json(403, {"ok": False, "message": "需要管理员权限"})
            conn = db()
            try:
                if "allowRegistration" in payload:
                    set_setting(conn, "allow_registration", "1" if payload.get("allowRegistration") else "0")
                    commit(conn)
                return self._json(200, {"ok": True, "message": "设置已保存", **public_settings(conn)})
            finally:
                conn.close()

        if path == "/api/admin/users":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            if not is_admin(sess["role"]):
                return self._json(403, {"ok": False, "message": "需要管理员权限"})
            code, body = admin_manage_account(sess["account_id"], payload)
            return self._json(code, body)

        if path == "/api/admin/login":
            ok, msg, data = login_admin(
                payload.get("username"),
                payload.get("password"),
                totp=payload.get("totp") or payload.get("otp") or payload.get("code") or "",
            )
            if not ok:
                body = {"ok": False, "message": msg}
                if isinstance(data, dict):
                    body.update(data)
                # 401 for bad password; 400 for missing second factor
                code = 400 if data and data.get("step") == "second_factor" else 401
                return self._json(code, body)
            return self._json(
                200,
                {
                    "ok": True,
                    "message": msg,
                    "token": data["token"],
                    "user": {
                        "username": data["username"],
                        "role": data["role"],
                        "accountId": data["accountId"],
                    },
                },
                self._session_cookie(data["token"]),
            )

        if path == "/api/admin/security":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            if not is_admin(sess["role"]):
                return self._json(403, {"ok": False, "message": "需要管理员权限"})
            action = str(payload.get("action") or "").strip().lower()
            conn = db()
            try:
                # ---- 2FA setup ----
                if action in ("2fa_begin", "begin2fa", "setup2fa"):
                    secret = new_totp_secret()
                    set_setting(conn, "admin_2fa_pending_secret", secret)
                    # do NOT enable yet
                    commit(conn)
                    return self._json(
                        200,
                        {
                            "ok": True,
                            "message": "已生成 2FA 密钥，请用验证器扫描后提交验证码确认",
                            "secret": secret,
                            "otpauth": otpauth_uri(secret, sess["username"]),
                            **admin_security_detail(conn),
                        },
                    )
                if action in ("2fa_confirm", "confirm2fa"):
                    pending = get_setting(conn, "admin_2fa_pending_secret", "")
                    if not pending:
                        return self._json(400, {"ok": False, "message": "没有待确认的 2FA 密钥，请先点「生成 2FA」"})
                    code = payload.get("totp") or payload.get("code") or ""
                    if not verify_totp(pending, code):
                        return self._json(400, {"ok": False, "message": "验证码错误，请重试"})
                    set_setting(conn, "admin_2fa_secret", pending)
                    set_setting(conn, "admin_2fa_pending_secret", "")
                    set_setting(conn, "admin_2fa_enabled", "1")
                    commit(conn)
                    return self._json(
                        200,
                        {
                            "ok": True,
                            "message": "2FA 已启用：之后管理员登录必须输入验证码",
                            **admin_security_detail(conn),
                        },
                    )
                if action in ("2fa_enable", "enable2fa"):
                    if not get_setting(conn, "admin_2fa_secret", ""):
                        return self._json(400, {"ok": False, "message": "请先生成并确认 2FA 密钥"})
                    set_setting(conn, "admin_2fa_enabled", "1")
                    commit(conn)
                    return self._json(200, {"ok": True, "message": "已开启 2FA 登录校验", **admin_security_detail(conn)})
                if action in ("2fa_disable", "disable2fa"):
                    # require current password to disable
                    row = conn.execute(
                        "SELECT password_salt, password_hash FROM accounts WHERE id=?",
                        (sess["account_id"],),
                    ).fetchone()
                    if not row or not verify_password(
                        payload.get("password") or "", row["password_salt"], row["password_hash"]
                    ):
                        return self._json(400, {"ok": False, "message": "关闭 2FA 需填写当前管理员密码"})
                    set_setting(conn, "admin_2fa_enabled", "0")
                    if payload.get("wipeSecret"):
                        set_setting(conn, "admin_2fa_secret", "")
                        set_setting(conn, "admin_2fa_pending_secret", "")
                    commit(conn)
                    return self._json(200, {"ok": True, "message": "已关闭 2FA 登录校验", **admin_security_detail(conn)})

                # ---- Removed legacy extra-factor actions ----
                if action in (
                    "keepass_set",
                    "setkeepass",
                    "keepass_enable",
                    "passkey_set",
                    "keepass_enable_only",
                    "enablekeepass",
                    "passkey_enable",
                    "keepass_disable",
                    "disablekeepass",
                    "passkey_disable",
                ):
                    set_setting(conn, "admin_keepass_enabled", "0")
                    set_setting(conn, "admin_keepass_salt", "")
                    set_setting(conn, "admin_keepass_hash", "")
                    commit(conn)
                    return self._json(
                        410,
                        {
                            "ok": False,
                            "message": "旧第二因子功能已删除；管理员登录只保留账号密码和 2FA。",
                            **admin_security_detail(conn),
                        },
                    )

                if action in ("change_password", "password"):
                    ok, msg = admin_set_password(
                        sess["account_id"],
                        payload.get("oldPassword") or payload.get("password") or "",
                        payload.get("newPassword") or "",
                    )
                    return self._json(200 if ok else 400, {"ok": ok, "message": msg})

                return self._json(
                    400,
                    {
                        "ok": False,
                        "message": "未知 action。可用：2fa_begin / 2fa_confirm / 2fa_enable / 2fa_disable / change_password",
                        **admin_security_detail(conn),
                    },
                )
            finally:
                conn.close()

        if path == "/api/sync":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录", "error": "not logged in"})
            envelope = payload.get("encryptedVault") or payload.get("envelope")
            if not isinstance(envelope, dict):
                return self._json(400, {"ok": False, "message": "缺少 encryptedVault（端到端加密包）"})
            # basic envelope sanity — server never decrypts
            if not (envelope.get("ciphertext") or envelope.get("cipherText")):
                return self._json(400, {"ok": False, "message": "加密包格式无效"})
            coverage = normalize_coverage(payload.get("coverage") if isinstance(payload.get("coverage"), dict) else {})
            records = int(coverage.get("records") or payload.get("records") or 0)
            now = int(time.time())
            conn = db()
            try:
                existing = conn.execute(
                    "SELECT envelope, coverage_json, records_count FROM encrypted_sync WHERE account_id=?",
                    (sess["account_id"],),
                ).fetchone()
                if existing:
                    conn.execute(
                        """INSERT INTO sync_backups(account_id, envelope, coverage_json, records_count, reason, created_at)
                           VALUES(?,?,?,?,?,?)""",
                        (
                            sess["account_id"],
                            existing["envelope"],
                            existing["coverage_json"] or "{}",
                            int(existing["records_count"] or 0),
                            "pre-sync",
                            now,
                        ),
                    )
                    purge_backups(conn, sess["account_id"])
                conn.execute(
                    """INSERT OR REPLACE INTO encrypted_sync(account_id, envelope, coverage_json, records_count, updated_at)
                       VALUES(?,?,?,?,?)""",
                    (
                        sess["account_id"],
                        json.dumps(envelope, ensure_ascii=False),
                        json.dumps(coverage, ensure_ascii=False),
                        records,
                        now,
                    ),
                )
                conn.execute("UPDATE accounts SET updated_at=? WHERE id=?", (now, sess["account_id"]))
                commit(conn)
                return self._json(
                    200,
                    {
                        "ok": True,
                        "message": "端到端加密同步成功",
                        "records": records,
                        "coverage": coverage,
                        "updatedAt": now,
                        "mode": "account-e2ee",
                    },
                )
            finally:
                conn.close()

        # soft stubs for old app reminder buttons (no plaintext on server)
        if path in ("/api/test-telegram", "/api/test-email", "/api/check-now"):
            return self._json(
                200,
                {
                    "ok": True,
                    "message": "E2EE 模式下提醒配置保存在客户端加密包中；请在 App 本地触发提醒。",
                },
            )

        if path == "/api/restore-backup":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            bid = int(payload.get("backupId") or payload.get("id") or 0)
            if bid <= 0:
                return self._json(400, {"ok": False, "message": "备份 ID 无效"})
            conn = db()
            try:
                row = conn.execute(
                    """SELECT envelope, coverage_json, records_count
                       FROM sync_backups WHERE account_id=? AND id=?""",
                    (sess["account_id"], bid),
                ).fetchone()
                if not row:
                    return self._json(404, {"ok": False, "message": "备份不存在"})
                now = int(time.time())
                # snapshot current before restore
                existing = conn.execute(
                    "SELECT envelope, coverage_json, records_count FROM encrypted_sync WHERE account_id=?",
                    (sess["account_id"],),
                ).fetchone()
                if existing:
                    conn.execute(
                        """INSERT INTO sync_backups(account_id, envelope, coverage_json, records_count, reason, created_at)
                           VALUES(?,?,?,?,?,?)""",
                        (
                            sess["account_id"],
                            existing["envelope"],
                            existing["coverage_json"] or "{}",
                            int(existing["records_count"] or 0),
                            "pre-restore",
                            now,
                        ),
                    )
                conn.execute(
                    """INSERT OR REPLACE INTO encrypted_sync(account_id, envelope, coverage_json, records_count, updated_at)
                       VALUES(?,?,?,?,?)""",
                    (
                        sess["account_id"],
                        row["envelope"],
                        row["coverage_json"] or "{}",
                        int(row["records_count"] or 0),
                        now,
                    ),
                )
                purge_backups(conn, sess["account_id"])
                commit(conn)
                return self._json(
                    200,
                    {
                        "ok": True,
                        "message": "已恢复指定加密备份，请在 App 拉取同步",
                        "records": int(row["records_count"] or 0),
                        "updatedAt": now,
                    },
                )
            finally:
                conn.close()

        if path == "/api/backups/clear":
            sess = self._session()
            if not sess:
                return self._json(401, {"ok": False, "message": "请先登录"})
            keep = max(0, min(200, int(payload.get("keep") or 20)))
            conn = db()
            try:
                purge_backups(conn, sess["account_id"], keep=keep)
                commit(conn)
                total = int(
                    conn.execute(
                        "SELECT count(*) c FROM sync_backups WHERE account_id=?",
                        (sess["account_id"],),
                    ).fetchone()["c"]
                    or 0
                )
                return self._json(
                    200,
                    {"ok": True, "message": "备份已清理", "total": total, "keep": keep},
                )
            finally:
                conn.close()

        if path.startswith("/api/"):
            return self._json(404, {"ok": False, "error": "not found"})

        return self._json(404, {"ok": False, "error": "not found"})

    # ---- static ----
    def _serve_static(self):
        path = self._path()
        if path in ("", "/"):
            path = "/index.html"
        elif path == "/admin" or path == "/admin/":
            path = "/admin.html"
        elif path == "/user" or path == "/user/":
            path = "/index.html"

        # security: no path traversal
        rel = path.lstrip("/")
        if ".." in rel or rel.startswith("/"):
            self.send_error(403)
            return
        file_path = (WEB_DIR / rel).resolve()
        if not str(file_path).startswith(str(WEB_DIR.resolve())):
            self.send_error(403)
            return
        if not file_path.is_file():
            # SPA-ish fallback for globe
            if path.startswith("/globe"):
                file_path = WEB_DIR / "index.html"
            else:
                self.send_error(404)
                return

        ctype = mimetypes.guess_type(str(file_path))[0] or "application/octet-stream"
        if file_path.suffix == ".html":
            ctype = "text/html; charset=utf-8"
        elif file_path.suffix == ".js":
            ctype = "application/javascript; charset=utf-8"
        elif file_path.suffix == ".json":
            ctype = "application/json; charset=utf-8"
        data = file_path.read_bytes()
        # gzip large HTML/JS/JSON when client accepts it (globe page is multi‑MB)
        accept_enc = (self.headers.get("Accept-Encoding") or "").lower()
        use_gzip = (
            "gzip" in accept_enc
            and file_path.suffix in (".html", ".js", ".json", ".css", ".svg")
            and len(data) > 2048
        )
        if use_gzip:
            import gzip

            data = gzip.compress(data, compresslevel=6)
        self.send_response(200)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(data)))
        if use_gzip:
            self.send_header("Content-Encoding", "gzip")
            self.send_header("Vary", "Accept-Encoding")
        # Static packaging model:
        # - index.html short cache (entry)
        # - vendor/app/assets/data long cache (browser disk = "local" after first visit)
        rel_posix = rel.replace("\\", "/")
        if file_path.name == "index.html" or rel_posix == "simj-portal.js":
            self.send_header("Cache-Control", "public, max-age=60")
        elif (
            rel_posix.startswith("vendor/")
            or rel_posix.startswith("app/")
            or rel_posix.startswith("assets/")
            or rel_posix.startswith("data/")
            or file_path.suffix in (".png", ".jpg", ".jpeg", ".webp", ".woff2", ".js", ".json")
        ):
            # 7 days; query ?v= on script tags busts when we ship new builds
            self.send_header("Cache-Control", "public, max-age=604800, immutable")
        else:
            self.send_header("Cache-Control", "no-cache")
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args), flush=True)


def main():
    init_db()
    # fresh schema: ignore legacy tables/files — user requested clean approach
    print(
        "simJ E2EE cloud %s listening on %s:%s  db=%s  web=%s"
        % (SERVICE_VERSION, HOST, PORT, DB, WEB_DIR),
        flush=True,
    )
    ThreadingHTTPServer((HOST, PORT), H).serve_forever()


if __name__ == "__main__":
    main()
