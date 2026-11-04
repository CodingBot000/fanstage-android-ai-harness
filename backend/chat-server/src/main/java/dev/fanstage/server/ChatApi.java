package dev.fanstage.server;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ChatApi {
    public record Message(String roomId, String messageId, String clientMessageId, long serverSequence, String text, boolean isOwn, String sentAt) {}
    private final ChatService service;
    private final DemoAuth auth;
    public ChatApi(ChatService service, DemoAuth auth) { this.service = service; this.auth = auth; }
    static Message view(ChatService.Stored m, String user) {
        boolean own = m.userId().equals(user);
        return new Message(m.roomId(), m.messageId().toString(), own ? m.clientMessageId().toString() : null, m.serverSequence(), m.text(), own, m.sentAt().toString());
    }
    @GetMapping("/health") public String health() { return "ok"; }
    @GetMapping("/rooms/{room}/messages")
    public List<Message> messages(@PathVariable String room, @RequestHeader(value="Authorization", required=false) String header,
        @RequestParam long afterSequence, @RequestParam long upperSequence, @RequestParam int limit) {
        String user = auth.user(header);
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        try { return service.history(room, afterSequence, upperSequence, limit).stream().map(m -> view(m, user)).toList(); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST); }
    }
}
