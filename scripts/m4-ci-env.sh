#!/usr/bin/env bash
# Called only in an isolated CI runner. Generated credentials are disposable.
set -euo pipefail
: "${RUNNER_TEMP:?}"
umask 077
export FANSTAGE_DB_USER=fanstage_ci
export FANSTAGE_DB_PASSWORD="$(openssl rand -hex 24)"
export FANSTAGE_TOKEN_ALICE="$(openssl rand -hex 32)"
export FANSTAGE_TOKEN_BOB="$(openssl rand -hex 32)"
if [ "${GITHUB_ACTIONS:-}" = true ]; then
  for value in "$FANSTAGE_DB_PASSWORD" "$FANSTAGE_TOKEN_ALICE" "$FANSTAGE_TOKEN_BOB"; do printf '::add-mask::%s\n' "$value"; done
fi
{
  printf 'export FANSTAGE_DB_USER=%q\n' "$FANSTAGE_DB_USER"
  printf 'export FANSTAGE_DB_PASSWORD=%q\n' "$FANSTAGE_DB_PASSWORD"
  printf 'export FANSTAGE_TOKEN_ALICE=%q\n' "$FANSTAGE_TOKEN_ALICE"
  printf 'export FANSTAGE_TOKEN_BOB=%q\n' "$FANSTAGE_TOKEN_BOB"
} > "$RUNNER_TEMP/fanstage-m4.env"
docker compose -p fanstage-m4-validation up --build -d
for attempt in {1..90}; do
  if curl --silent --fail --max-time 2 http://127.0.0.1:18765/health >/dev/null; then exit 0; fi
  sleep 2
done
printf '%s\n' 'FAIL: local chat server did not become healthy' >&2
exit 1
