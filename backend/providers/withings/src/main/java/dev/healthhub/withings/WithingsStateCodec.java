package dev.healthhub.withings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class WithingsStateCodec {

    private final byte[] secret;

    WithingsStateCodec(String callbackSecret) {
        if (callbackSecret == null || callbackSecret.isBlank()) {
            throw new IllegalStateException("healthhub.withings.callback-secret (WITHINGS_CALLBACK_SECRET) must be set");
        }
        this.secret = callbackSecret.getBytes(StandardCharsets.UTF_8);
    }

    String issue(UUID connectionId) {
        var nonce = UUID.randomUUID().toString();
        return connectionId + "." + nonce + "." + hmac(connectionId + "." + nonce);
    }

    UUID verify(String state) {
        var parts = state.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("malformed state");
        }
        var expected = hmac(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("state signature mismatch");
        }
        return UUID.fromString(parts[0]);
    }

    private String hmac(String value) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            var out = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder(out.length * 2);
            for (byte b : out) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC unavailable", e);
        }
    }
}
