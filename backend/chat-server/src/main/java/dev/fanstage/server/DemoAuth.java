package dev.fanstage.server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Local demo only: token identifies a fixed synthetic user; never log tokens. */
@Component
public class DemoAuth {
    private final byte[] alice;
    private final byte[] bob;
    public DemoAuth(@Value("${FANSTAGE_TOKEN_ALICE}") String alice, @Value("${FANSTAGE_TOKEN_BOB}") String bob) {
        if (alice.length() < 32 || bob.length() < 32 || alice.equals(bob)) throw new IllegalArgumentException("Distinct demo tokens of at least 32 characters are required");
        this.alice = alice.getBytes(StandardCharsets.UTF_8);
        this.bob = bob.getBytes(StandardCharsets.UTF_8);
    }
    public String user(String header) {
        if (header == null || !header.startsWith("Bearer ")) return null;
        byte[] candidate = header.substring(7).getBytes(StandardCharsets.UTF_8);
        boolean a = MessageDigest.isEqual(alice, candidate);
        boolean b = MessageDigest.isEqual(bob, candidate);
        return a ? "alice" : b ? "bob" : null;
    }
}
