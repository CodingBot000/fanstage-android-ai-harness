package dev.fanstage.server;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    public record Stored(UUID messageId, String roomId, long serverSequence, String userId, UUID clientMessageId, String text, Instant sentAt) {}
    public record Result(Stored message, boolean created) {}
    public static class Conflict extends RuntimeException {}
    private final JdbcTemplate db;
    public ChatService(JdbcTemplate db) { this.db = db; }
    public static boolean validRoom(String room) { return room != null && room.matches("[a-z0-9][a-z0-9-]{0,63}"); }
    public static boolean validText(String text) {
        if (text == null || text.isBlank() || text.indexOf('\0') >= 0 || text.codePointCount(0, text.length()) > 1000) return false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (++i >= text.length() || !Character.isLowSurrogate(text.charAt(i))) return false;
            } else if (Character.isLowSurrogate(c)) return false;
        }
        return true;
    }
    private static final org.springframework.jdbc.core.RowMapper<Stored> MAPPER = (rs, n) -> new Stored(
        rs.getObject("message_id", UUID.class), rs.getString("room_id"), rs.getLong("server_sequence"),
        rs.getString("user_id"), rs.getObject("client_message_id", UUID.class), rs.getString("body"), rs.getTimestamp("created_at").toInstant());

    /** Row lock serializes both sequence allocation and idempotency per room; commit occurs at proxy return. */
    @Transactional
    public Result send(String room, String user, UUID clientId, String text) {
        if (!validRoom(room) || user == null || clientId == null || !validText(text)) throw new IllegalArgumentException("Invalid request");
        db.update("INSERT INTO room_counters(room_id,next_sequence) VALUES (?,1) ON CONFLICT DO NOTHING", room);
        long next = db.queryForObject("SELECT next_sequence FROM room_counters WHERE room_id=? FOR UPDATE", Long.class, room);
        var existing = db.query("SELECT * FROM chat_messages WHERE room_id=? AND user_id=? AND client_message_id=?", MAPPER, room, user, clientId);
        if (!existing.isEmpty()) {
            if (!existing.get(0).text().equals(text)) throw new Conflict();
            return new Result(existing.get(0), false);
        }
        var stored = db.queryForObject("INSERT INTO chat_messages(message_id,room_id,server_sequence,user_id,client_message_id,body) VALUES (?,?,?,?,?,?) RETURNING *",
            MAPPER, UUID.randomUUID(), room, next, user, clientId, text);
        db.update("UPDATE room_counters SET next_sequence=? WHERE room_id=?", next + 1, room);
        return new Result(stored, true);
    }
    public long head(String room) {
        Long head = db.queryForObject("SELECT COALESCE(MAX(server_sequence),0) FROM chat_messages WHERE room_id=?", Long.class, room);
        return head == null ? 0 : head;
    }
    public List<Stored> history(String room, long after, long upper, int limit) {
        if (!validRoom(room) || after < 0 || upper < after || limit < 1 || limit > 1000) throw new IllegalArgumentException("Invalid range");
        return db.query("SELECT * FROM chat_messages WHERE room_id=? AND server_sequence>? AND server_sequence<=? ORDER BY server_sequence LIMIT ?", MAPPER, room, after, upper, limit);
    }
}
