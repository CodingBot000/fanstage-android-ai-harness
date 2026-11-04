CREATE TABLE room_counters (
    room_id VARCHAR(64) PRIMARY KEY,
    next_sequence BIGINT NOT NULL CHECK (next_sequence > 0)
);
CREATE TABLE chat_messages (
    message_id UUID PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL REFERENCES room_counters(room_id),
    server_sequence BIGINT NOT NULL CHECK (server_sequence > 0),
    user_id VARCHAR(32) NOT NULL,
    client_message_id UUID NOT NULL,
    body TEXT NOT NULL CHECK (char_length(body) BETWEEN 1 AND 1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT room_sequence_unique UNIQUE (room_id, server_sequence),
    CONSTRAINT room_user_client_unique UNIQUE (room_id, user_id, client_message_id)
);
