# 로컬 채팅·미디어 서버 실행

FanStage의 로컬 테스트용 서버는 **Spring Boot + PostgreSQL**로 구성됩니다. WebSocket 실시간 이벤트와 HTTP 메시지 조회를 제공하며, Android Debug 앱에서 연동할 수 있습니다. 개발용 토큰과 로컬 HTTP 연결을 사용하므로 외부 인터넷에 공개하지 않고 실행합니다.

## 1. 로컬 채팅 서버 실행

저장소 루트에서 일회용 테스트 자격값을 생성합니다.

~~~bash
export FANSTAGE_DB_USER=fanstage_local
export FANSTAGE_DB_PASSWORD="$(openssl rand -hex 24)"
export FANSTAGE_TOKEN_ALICE="$(openssl rand -hex 32)"
export FANSTAGE_TOKEN_BOB="$(openssl rand -hex 32)"
docker compose up --build -d
~~~

Android 에뮬레이터의 Debug 앱에서 **http://10.0.2.2:18765**에 연결하고 LIVE 화면 개발 설정에 테스트 토큰 하나를 입력합니다.

서버 사용을 마치면 다음 명령으로 중지합니다.

~~~bash
docker compose down
~~~

일반 `down`은 PostgreSQL 데이터 볼륨을 삭제하지 않습니다.

## 2. 격리된 서버 통합·장애 테스트

**앞 단계에서 실행한 서버를 먼저 중지한 후** 테스트를 실행하세요. 두 Compose 프로젝트를 동시에 실행하면 기본 포트 `18765`가 충돌합니다.

테스트 스크립트는 합성 메시지를 만들고 DB에 테스트용 실패 트리거를 설치하거나 서버를 재시작하므로, 반드시 별도의 테스트 프로젝트를 사용합니다.

~~~bash
# 기존 기본 서버가 실행 중이라면 먼저 중지
docker compose down

# 격리된 테스트 프로젝트 실행
docker compose -p fanstage-m4-validation up --build -d
python3 backend/chat-server/tests/integration.py
python3 backend/chat-server/tests/m5_faults.py

# 테스트가 끝나면 격리 프로젝트 종료
docker compose -p fanstage-m4-validation down
~~~

테스트 프로젝트의 데이터 볼륨도 일반 `down` 이후에는 남습니다. 별도로 볼륨을 제거할 때는 필요한 데이터가 없는지 확인하세요.

테스트 대상은 메시지 순번·멱등 재전송, WebSocket/HTTP 통합, ACK 유실과 네트워크 복구입니다. [채팅 계약](../../docs/specs/realtime-chat.md) · [장애 주입 시나리오](../../docs/specs/failure-recovery.md).

## 3. 선택형 로컬 MP4 시연

`demo-01.mp4`부터 `demo-07.mp4`까지의 사전 녹화 영상을 저장소 외부의 개인 디렉터리에 준비합니다. 파일은 Git에 포함하지 않습니다. 다른 서버가 `18765` 포트를 사용 중이라면 먼저 종료하세요.

~~~bash
export FANSTAGE_MEDIA_HOST_DIR=/path/to/private/media
export FANSTAGE_BIND_ADDRESS=127.0.0.1
docker compose -f compose.yaml -f compose.media.yaml -p fanstage-media-demo up --build -d

curl -I http://127.0.0.1:18765/demo-media/demo-01.mp4
curl -sS -D - -o /dev/null -H 'Range: bytes=0-1023' http://127.0.0.1:18765/demo-media/demo-01.mp4
~~~

USB로 연결한 실기기에서 사용할 때는 `adb reverse tcp:18765 tcp:18765`로 호스트의 로컬 포트를 전달할 수 있습니다. 해당 기기의 Debug 연결 주소는 `127.0.0.1`을 사용합니다.

미디어 Compose 설정은 서비스를 `127.0.0.1`에만 바인딩합니다. Docker Compose의 `!override` 기능을 지원하는 버전이 필요합니다. 로컬 MP4는 HTTP Progressive VOD이며 실제 생방송 HLS 송출 서버는 아닙니다.
