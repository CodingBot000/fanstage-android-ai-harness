package dev.fanstage.server;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
@EnableWebSocket
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer, HandshakeInterceptor {
    record Peer(WebSocketSession session, String room, String user) {}
    record Envelope(String type, Long headSequence, ChatApi.Message message, String clientMessageId, String code) {}
    private final DemoAuth auth;
    private final ChatService service;
    private final JsonMapper json;
    private final Map<String, Peer> peers = new ConcurrentHashMap<>();
    public ChatSocket(DemoAuth auth, ChatService service, JsonMapper json) { this.auth = auth; this.service = service; this.json = json; }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { registry.addHandler(this, "/ws/rooms/*").addInterceptors(this); }
    @Override public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Map<String,Object> attributes) {
        String user = auth.user(request.getHeaders().getFirst("Authorization"));
        String path = request.getURI().getPath();
        String room = path.substring(path.lastIndexOf('/') + 1);
        if (user == null) { response.setStatusCode(HttpStatus.UNAUTHORIZED); return false; }
        if (!ChatService.validRoom(room)) { response.setStatusCode(HttpStatus.BAD_REQUEST); return false; }
        attributes.put("user", user); attributes.put("room", room); return true;
    }
    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception ex) {}
    @Override public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setTextMessageSizeLimit(8192);
        var decorated = new ConcurrentWebSocketSessionDecorator(session, 5000, 1_048_576);
        var peer = new Peer(decorated, (String)session.getAttributes().get("room"), (String)session.getAttributes().get("user"));
        peers.put(session.getId(), peer); // subscription first; head captures all commits before it
        try { send(peer, new Envelope("subscribed", service.head(peer.room()), null, null, null)); }
        catch (RuntimeException e) { peers.remove(session.getId()); throw e; }
    }
    @Override protected void handleTextMessage(WebSocketSession session, TextMessage text) {
        Peer peer = peers.get(session.getId()); if (peer == null) return;
        String clientId = null;
        try {
            JsonNode n = json.readTree(text.getPayload());
            if (!n.isObject() || !"send".equals(n.path("type").asText()) || !n.path("clientMessageId").isString() || !n.path("text").isString()) throw new IllegalArgumentException();
            clientId = n.path("clientMessageId").asText();
            UUID id = UUID.fromString(clientId);
            if (!id.toString().equals(clientId)) throw new IllegalArgumentException();
            var result = service.send(peer.room(), peer.user(), id, n.path("text").asText());
            // @Transactional service proxy returns only after the database commit succeeds.
            send(peer, new Envelope("ack", null, ChatApi.view(result.message(), peer.user()), null, null));
            if (result.created()) {
                for (Peer recipient : peers.values()) if (recipient.room().equals(peer.room()))
                    send(recipient, new Envelope("message", null, ChatApi.view(result.message(), recipient.user()), null, null));
            }
        } catch (tools.jackson.core.JacksonException e) { send(peer, new Envelope("error", null, null, clientId, "invalid")); }
        catch (ChatService.Conflict e) { send(peer, new Envelope("error", null, null, clientId, "conflict")); }
        catch (IllegalArgumentException e) { send(peer, new Envelope("error", null, null, clientId, "invalid")); }
        catch (Exception e) { send(peer, new Envelope("error", null, null, clientId, "unknown")); }
    }
    private void send(Peer peer, Envelope message) {
        try { peer.session().sendMessage(new TextMessage(json.writeValueAsString(message))); }
        catch (Exception e) { try { peer.session().close(CloseStatus.SERVER_ERROR); } catch (IOException ignored) {} }
    }
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) { peers.remove(session.getId()); }
    @Override public void handleTransportError(WebSocketSession session, Throwable error) throws Exception { peers.remove(session.getId()); session.close(CloseStatus.SERVER_ERROR); }
}
