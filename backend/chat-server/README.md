# FanStage · Local Chat Server

Development-only Spring Boot + PostgreSQL WebSocket/HTTP backend. Use synthetic test users and disposable credentials. HTTP/WebSocket connections are not production authentication or TLS.

## Start local integration backend

From repository root, generate temporary credentials in your local shell:

~~~bash
export FANSTAGE_DB_USER=fanstage_local
export FANSTAGE_DB_PASSWORD="$(openssl rand -hex 24)"
export FANSTAGE_TOKEN_ALICE="$(openssl rand -hex 32)"
export FANSTAGE_TOKEN_BOB="$(openssl rand -hex 32)"
docker compose up --build -d
~~~

Android Emulator Debug can connect to **http://10.0.2.2:18765** and select the local server through the LIVE room development controls. Enter one test token. Release traffic does not permit cleartext; the demo backend should not be exposed publicly.

Use an **isolated disposable project** for the integration tests. The suite changes test data, adds a deferred-failure trigger and restarts the validation server/database:

~~~bash
docker compose -p fanstage-m4-validation up --build -d
python3 backend/chat-server/tests/integration.py
python3 backend/chat-server/tests/m5_faults.py
docker compose -p fanstage-m4-validation down
~~~

Docker volumes persist until explicitly removed. Do not point test scripts to a database whose data must be preserved. [Chat contract](../../docs/specs/realtime-chat.md) · [Fault injection](../../docs/specs/failure-recovery.md).

## Optional local MP4 media

Place private **demo-01.mp4** through **demo-07.mp4** in a directory outside this repository and set FANSTAGE_MEDIA_HOST_DIR. Avoid using another Compose project on the same port.

~~~bash
export FANSTAGE_MEDIA_HOST_DIR=/path/to/private/media
export FANSTAGE_BIND_ADDRESS=127.0.0.1
docker compose -f compose.yaml -f compose.media.yaml -p fanstage-media-demo up --build -d
curl -I http://127.0.0.1:18765/demo-media/demo-01.mp4
curl -sS -D - -o /dev/null -H 'Range: bytes=0-1023' http://127.0.0.1:18765/demo-media/demo-01.mp4
~~~

The Debug application exposes local prerecorded video via the Media/Replay routes; **Replay does not offer live chat**. The local LIVE-themed room provides a separate chat view. MP4 is served over Progressive HTTP and is not a live HLS stream.

The optional media Compose override binds the service to 127.0.0.1 even if the base bind address differs. This uses the Docker Compose !override feature (a recent version is required). For a USB-connected device you can use adb reverse to forward the local server port without opening the server to a LAN.
