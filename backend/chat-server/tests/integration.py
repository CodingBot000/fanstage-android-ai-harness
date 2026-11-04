#!/usr/bin/env python3
"""Local Compose integration: real WebSocket/HTTP/PostgreSQL; no test tokens are logged."""
import base64
import concurrent.futures
import hashlib
import json
import os
import socket
import struct
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

BASE = "http://127.0.0.1:18765"
A, B = os.environ["FANSTAGE_TOKEN_ALICE"], os.environ["FANSTAGE_TOKEN_BOB"]
ROOM = "test-" + uuid.uuid4().hex[:16]


def http(path, token=None):
    request = urllib.request.Request(BASE + path, headers={"Authorization": "Bearer " + token} if token else {})
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.load(response)


def history(token, after=0, upper=9223372036854775807, limit=1000, room=ROOM):
    return http(f"/rooms/{room}/messages?afterSequence={after}&upperSequence={upper}&limit={limit}", token)


class WS:
    def __init__(self, token, room=ROOM):
        self.s = socket.create_connection(("127.0.0.1", 18765), timeout=10)
        key = base64.b64encode(os.urandom(16)).decode()
        req = (f"GET /ws/rooms/{room} HTTP/1.1\r\nHost: 127.0.0.1:18765\r\nUpgrade: websocket\r\n"
               f"Connection: Upgrade\r\nSec-WebSocket-Key: {key}\r\nSec-WebSocket-Version: 13\r\nAuthorization: Bearer {token}\r\n\r\n")
        self.s.sendall(req.encode())
        head = b""
        while not head.endswith(b"\r\n\r\n"):
            head += self.s.recv(1)
            if len(head) > 16384: raise AssertionError("handshake too large")
        if b" 101 " not in head.split(b"\r\n", 1)[0]:
            self.s.close()
            raise AssertionError("handshake rejected")
        expected = base64.b64encode(hashlib.sha1((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").encode()).digest())
        assert expected.lower() in head.lower()
        self.early = []
        for _ in range(100):
            packet = self.recv()
            if packet["type"] == "subscribed":
                self.head = packet["headSequence"]
                break
            self.early.append(packet)
        else: raise AssertionError("subscription acknowledgement not received")

    def send(self, payload):
        data = json.dumps(payload, ensure_ascii=False).encode()
        mask = os.urandom(4)
        length = len(data)
        prefix = bytes([0x81, 0x80 | length]) if length < 126 else bytes([0x81, 0x80 | 126]) + struct.pack("!H", length)
        self.s.sendall(prefix + mask + bytes(b ^ mask[i % 4] for i, b in enumerate(data)))

    def recv(self):
        def exact(n):
            result = b""
            while len(result) < n:
                chunk = self.s.recv(n - len(result))
                if not chunk: raise EOFError("socket closed")
                result += chunk
            return result
        head = exact(2); size = head[1] & 127
        if size == 126: size = struct.unpack("!H", exact(2))[0]
        if size == 127: size = struct.unpack("!Q", exact(8))[0]
        if head[1] & 128: raise AssertionError("server frame must not be masked")
        data = exact(size)
        if head[0] & 15 != 1: raise AssertionError("unexpected websocket frame")
        return json.loads(data)

    def until(self, kind):
        for _ in range(100):
            obj = self.recv()
            if obj["type"] == kind: return obj
        raise AssertionError("expected frame not received")

    def close(self): self.s.close()


def send(ws, id_, text):
    ws.send({"type": "send", "clientMessageId": id_, "text": text})


def main():
    try: http(f"/rooms/{ROOM}/messages?afterSequence=0&upperSequence=1&limit=1")
    except urllib.error.HTTPError as e: assert e.code == 401
    else: raise AssertionError("anonymous history allowed")
    try: WS("invalid-test-credential")
    except AssertionError: pass
    else: raise AssertionError("invalid WebSocket credential allowed")
    # A message committed before registration is in the snapshot; one committed
    # after registration but before the HTTP read is in the live stream.
    boundary_room = "barrier-" + uuid.uuid4().hex[:16]
    sender = WS(A, boundary_room)
    send(sender, str(uuid.uuid4()), "before subscription")
    assert sender.until("ack")["message"]["serverSequence"] == 1
    observer = WS(B, boundary_room)
    assert observer.head == 1
    send(sender, str(uuid.uuid4()), "during catchup")
    assert sender.until("ack")["message"]["serverSequence"] == 2
    live = observer.until("message")["message"]
    snapshot = history(B, 0, observer.head, 1000, room=boundary_room)
    assert [m["serverSequence"] for m in snapshot] + [live["serverSequence"]] == [1, 2]
    sender.close(); observer.close()
    print("PASS: subscription and HTTP snapshot boundary retains both messages", flush=True)
    # Concurrent retries of one client ID must converge to one row and sequence.
    duplicate_room = "idem-" + uuid.uuid4().hex[:16]
    duplicate_id = str(uuid.uuid4())
    def duplicate_send(_):
        ws = WS(A, duplicate_room)
        try:
            send(ws, duplicate_id, "concurrent-retry")
            return ws.until("ack")["message"]
        finally: ws.close()
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        copies = list(pool.map(duplicate_send, range(12)))
    assert len({m["messageId"] for m in copies}) == 1
    assert {m["serverSequence"] for m in copies} == {1}
    assert len(history(A, room=duplicate_room)) == 1
    print("PASS: concurrent same-ID retries produce one durable message", flush=True)
    unicode_room = "unicode-" + uuid.uuid4().hex[:16]
    unicode_ws = WS(A, unicode_room)
    send(unicode_ws, str(uuid.uuid4()), "😀" * 1000)
    assert len(unicode_ws.until("ack")["message"]["text"]) == 1000
    unicode_ws.until("message")
    send(unicode_ws, str(uuid.uuid4()), "😀" * 1001)
    assert unicode_ws.until("error")["code"] == "invalid"
    send(unicode_ws, "invalid-id", "message")
    assert unicode_ws.until("error")["code"] == "invalid"
    assert len(history(A, room=unicode_room)) == 1
    unicode_ws.close()
    print("PASS: Unicode code-point boundary and invalid ID", flush=True)
    alice, bob = WS(A), WS(B)
    first = str(uuid.uuid4()); alice.send({"type": "send", "clientMessageId": first, "text": "synthetic-hello", "isOwn": False})
    ack = alice.until("ack")["message"]; echo = alice.until("message")["message"]; received = bob.until("message")["message"]
    assert ack["messageId"] == echo["messageId"] and ack["serverSequence"] == 1
    assert ack["isOwn"] and received["isOwn"] is False and received["clientMessageId"] is None
    assert ack["sentAt"].endswith("Z") and received["sentAt"] == ack["sentAt"]
    assert history(A)[0]["messageId"] == ack["messageId"]  # row visible after ACK
    send(bob, first, "bob-same-id"); bob_ack = bob.until("ack")["message"]
    assert bob_ack["isOwn"] and bob_ack["messageId"] != ack["messageId"] and bob_ack["serverSequence"] == 2
    bob_echo = bob.until("message")["message"]; assert bob_echo["isOwn"]
    bob_seen_by_alice = alice.until("message")["message"]
    assert bob_seen_by_alice["isOwn"] is False and bob_seen_by_alice["clientMessageId"] is None
    send(alice, first, "synthetic-hello"); duplicate = alice.until("ack")["message"]
    assert duplicate["messageId"] == ack["messageId"] and duplicate["sentAt"] == ack["sentAt"] and len(history(A)) == 2
    send(alice, first, "different"); assert alice.until("error")["code"] == "conflict"
    send(alice, str(uuid.uuid4()), "x" * 1001); assert alice.until("error")["code"] == "invalid"
    send(alice, str(uuid.uuid4()), "bad\u0000text"); assert alice.until("error")["code"] == "invalid"
    assert len(history(A)) == 2
    for query in ("afterSequence=2&upperSequence=1&limit=1", "afterSequence=0&upperSequence=1&limit=1001"):
        try: http(f"/rooms/{ROOM}/messages?{query}", A)
        except urllib.error.HTTPError as e: assert e.code == 400
        else: raise AssertionError("invalid history range allowed")
    alice.close(); bob.close()

    def concurrent_send(i):
        ws = WS(A)
        try:
            send(ws, str(uuid.uuid4()), "parallel-" + str(i))
            return ws.until("ack")["message"]["serverSequence"]
        finally: ws.close()
    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as pool:
        sequences = list(pool.map(concurrent_send, range(20)))
    assert sorted(sequences) == list(range(3, 23))
    assert [m["serverSequence"] for m in history(A)] == list(range(1, 23))
    assert [m["serverSequence"] for m in history(A, 5, 10, 3)] == [6, 7, 8]
    print("PASS: authenticated WS, ACK/broadcast, idempotency/conflict, length, concurrent sequences and HTTP gaps", flush=True)

    # This disposable Compose project has a test-only deferred trigger to fail at COMMIT.
    sql = """CREATE FUNCTION fanstage_test_abort() RETURNS trigger LANGUAGE plpgsql AS $$
    BEGIN IF NEW.body = 'synthetic-commit-abort' THEN RAISE EXCEPTION 'synthetic commit abort'; END IF; RETURN NEW; END $$;
    CREATE CONSTRAINT TRIGGER fanstage_test_abort_trigger AFTER INSERT ON chat_messages
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fanstage_test_abort();"""
    subprocess.run(["docker", "exec", "-i", "fanstage-m4-validation-db-1", "psql", "-U", os.environ["FANSTAGE_DB_USER"], "-d", "fanstage", "-v", "ON_ERROR_STOP=1"], input=sql, text=True, check=True, stdout=subprocess.DEVNULL)
    ws = WS(A); bad = str(uuid.uuid4()); send(ws, bad, "synthetic-commit-abort")
    while True:
        result = ws.recv()
        if result["type"] in ("ack", "message"): raise AssertionError("ACK or broadcast observed before failing COMMIT")
        if result["type"] == "error": break
    assert result["code"] == "unknown"
    assert len(history(A)) == 22
    cleanup = "DROP TRIGGER fanstage_test_abort_trigger ON chat_messages; DROP FUNCTION fanstage_test_abort();"
    subprocess.run(["docker", "exec", "-i", "fanstage-m4-validation-db-1", "psql", "-U", os.environ["FANSTAGE_DB_USER"], "-d", "fanstage", "-v", "ON_ERROR_STOP=1"], input=cleanup, text=True, check=True, stdout=subprocess.DEVNULL)
    send(ws, bad, "after-rollback"); assert ws.until("ack")["message"]["serverSequence"] == 23
    ws.close(); print("PASS: deferred COMMIT failure produced no ACK and rolled back sequence", flush=True)

    subprocess.run(["docker", "compose", "-p", "fanstage-m4-validation", "restart", "db", "chat"], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    for _ in range(60):
        try:
            with urllib.request.urlopen(BASE + "/health", timeout=1) as r:
                if r.status == 200: break
        except (OSError, urllib.error.URLError): time.sleep(1)
    else: raise AssertionError("server restart did not become healthy")
    assert len(history(A)) == 23
    ws = WS(A); send(ws, first, "synthetic-hello"); after = ws.until("ack")["message"]; ws.close()
    assert after["messageId"] == ack["messageId"] and after["serverSequence"] == 1 and after["sentAt"] == ack["sentAt"] and len(history(A)) == 23
    print("PASS: server restart preserved message ID, sequence and idempotent result", flush=True)


if __name__ == "__main__": main()
