package dev.healthhub.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtDecoderConfigTest {

    @Test
    void decoderAcceptsTokenSignedWithConfiguredKey() throws Exception {
        var keyBytes = new byte[48];
        new SecureRandom().nextBytes(keyBytes);
        var base64Key = Base64.getEncoder().encodeToString(keyBytes);

        var claims = new JWTClaimsSet.Builder()
                .subject("test-user")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();
        var signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signed.sign(new MACSigner(keyBytes));

        var decoder = new SecurityConfig().jwtDecoder(base64Key);
        var jwt = decoder.decode(signed.serialize());

        assertEquals("test-user", jwt.getSubject());
    }
}
