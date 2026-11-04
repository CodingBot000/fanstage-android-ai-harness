# Local chat server

Development-only Spring Boot + PostgreSQL. Use synthetic messages and disposable test tokens; do not use production credentials or expose this service to the public internet. The database volume persists across an ordinary `docker compose down`. No automatic data expiry is configured.

From the repository root, create fresh credentials in the current shell without printing them:

```sh
export FANSTAGE_DB_USER=fanstage_local
export FANSTAGE_DB_PASSWORD="$(openssl rand -hex 24)"
export FANSTAGE_TOKEN_ALICE="$(openssl rand -hex 32)"
export FANSTAGE_TOKEN_BOB="$(openssl rand -hex 32)"
docker compose up --build -d
```

Debug Android defaults to `http://10.0.2.2:18765` for the emulator. In the room's development controls choose **Use local server** and enter one temporary token. For a physical device on an isolated trusted LAN, explicitly set `FANSTAGE_BIND_ADDRESS` to the host's LAN address before starting Compose, use that address in the debug UI, and restrict network access. HTTP/WS and the tokens are plaintext on the network; this is not production authentication. Release builds do not enable cleartext traffic.

To run the reproducible server integration suite, use a **dedicated disposable validation project** with the same exported credentials:

```sh
docker compose -p fanstage-m4-validation up --build -d
python3 backend/chat-server/tests/integration.py
```

The suite creates synthetic rooms, installs a temporary deferred-failure trigger and restarts the validation database and server. Do not run it against a database containing data you need. Stop a project with `docker compose -p fanstage-m4-validation down`; data volumes are not removed by that command. Removing volumes destroys the stored data and must be deliberate.

See [M4 SDD](../../docs/specs/M4-real-chat.md) for protocol, limits, validation and remaining risks.
