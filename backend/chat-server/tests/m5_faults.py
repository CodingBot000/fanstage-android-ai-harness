#!/usr/bin/env python3
"""M5 NET-01/02: isolated, test-only frame proxy; no server fault endpoint."""
import json
import socket
import struct
import threading
import time
import uuid
from integration import WS, A, B, PORT, history, send

SCENARIO = 'm5-net02-seed-73'

def exact(sock, count):
    data = b''
    while len(data) < count:
        part = sock.recv(count - len(data))
        if not part:
            raise EOFError('closed')
        data += part
    return data

class DropFirstAck:
    def __init__(self):
        self.listener = socket.socket()
        self.listener.bind(('127.0.0.1', 0))
        self.listener.listen(1)
        self.port = self.listener.getsockname()[1]
        self.dropped = threading.Event()
        self.threads = []
        self.worker = threading.Thread(target=self.run, daemon=True)
        self.worker.start()

    def run(self):
        client, _ = self.listener.accept()
        upstream = socket.socket(); upstream.connect(('127.0.0.1', PORT))
        client.settimeout(None); upstream.settimeout(None)
        self.sockets = (client, upstream)
        def upstream_copy():
            try:
                while data := client.recv(8192): upstream.sendall(data)
            except OSError: pass
            finally: upstream.shutdown(socket.SHUT_WR) if upstream.fileno() != -1 else None
        self.threads.append(threading.Thread(target=upstream_copy, daemon=True))
        self.threads[0].start()
        try:
            header = b''
            while not header.endswith(b'\r\n\r\n'): header += exact(upstream, 1)
            client.sendall(header)
            while True:
                head = exact(upstream, 2); size = head[1] & 127; extension = b''
                if size == 126: extension = exact(upstream, 2); size = struct.unpack('!H', extension)[0]
                elif size == 127: extension = exact(upstream, 8); size = struct.unpack('!Q', extension)[0]
                payload = exact(upstream, size)
                packet = json.loads(payload) if head[0] & 15 == 1 else {}
                if packet.get('type') == 'ack' and not self.dropped.is_set():
                    self.dropped.set()
                    continue
                client.sendall(head + extension + payload)
        except (OSError, EOFError): pass
        finally:
            for sock in (client, upstream): sock.close()

    def close(self):
        self.listener.close()
        for sock in getattr(self, 'sockets', ()): sock.close()
        self.worker.join(2)


def connect_through_proxy(token, room, port):
    # Reuse the production-independent WS client while directing only its TCP
    # connection through the test proxy. Requests and auth are never logged.
    original = socket.create_connection
    def route(address, *args, **kwargs):
        return original(('127.0.0.1', port), *args, **kwargs)
    socket.create_connection = route
    try: return WS(token, room)
    finally: socket.create_connection = original


def main():
    room = 'fault-' + uuid.uuid4().hex[:16]
    proxy = DropFirstAck()
    observer = WS(B, room)
    sender = connect_through_proxy(A, room, proxy.port)
    msg_id = str(uuid.UUID(int=73))
    try:
        send(sender, msg_id, 'synthetic-ack-loss')
        first = observer.until('message')['message']
        assert proxy.dropped.wait(5), 'ACK was not dropped by proxy'
        assert len(history(A, room=room)) == 1, 'commit not visible after dropped ACK'
        send(sender, msg_id, 'synthetic-ack-loss')
        retry = sender.until('ack')['message']
        assert retry['messageId'] == first['messageId']
        assert len(history(A, room=room)) == 1
        observer.s.settimeout(1)
        try:
            unexpected = observer.recv()
            raise AssertionError('duplicate broadcast: ' + unexpected['type'])
        except socket.timeout: pass
        print('PASS:', SCENARIO, 'committed ACK dropped; same ID retry: one row, one broadcast', flush=True)
    finally:
        sender.close(); observer.close(); proxy.close()

    # Disconnect one subscriber for ten wall-clock seconds while the other user
    # commits. A new subscription and its bounded HTTP snapshot must recover it.
    room = 'offline-' + uuid.uuid4().hex[:16]
    offline = WS(A, room); offline.close()
    connected = WS(B, room)
    began = time.monotonic()
    send(connected, str(uuid.UUID(int=74)), 'synthetic-offline')
    assert connected.until('ack')['message']['serverSequence'] == 1
    while time.monotonic() - began < 10:
        time.sleep(max(0, min(0.1, 10 - (time.monotonic() - began))))
    reconnected = WS(A, room)
    try:
        assert reconnected.head == 1
        restored = history(A, 0, reconnected.head, room=room)
        assert len(restored) == 1 and restored[0]['text'] == 'synthetic-offline'
        print('PASS: m5-net01-seed-73 10s disconnect; HTTP snapshot recovered committed event', flush=True)
    finally:
        connected.close(); reconnected.close()

if __name__ == '__main__': main()
