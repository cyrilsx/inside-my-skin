package dev.healthhub.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class TokenCipherTest {

    private static TokenCipher cipherWithRandomKey() {
        var keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        return new TokenCipher(Base64.getEncoder().encodeToString(keyBytes));
    }

    @Test
    void encryptDecryptRoundTrips() {
        var cipher = cipherWithRandomKey();
        var token = "super-secret-access-token-value";
        var encrypted = cipher.encrypt(token);
        assertNotEquals(token, encrypted);
        assertEquals(token, cipher.decrypt(encrypted));
    }

    @Test
    void ciphertextDiffersPerCall() {
        var cipher = cipherWithRandomKey();
        assertNotEquals(cipher.encrypt("same"), cipher.encrypt("same"));
    }

    @Test
    void tamperedCiphertextIsRejected() {
        var cipher = cipherWithRandomKey();
        var encrypted = cipher.encrypt("token");
        var bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 0x01;
        var tampered = Base64.getEncoder().encodeToString(bytes);
        assertThrows(IllegalStateException.class, () -> cipher.decrypt(tampered));
    }
}
