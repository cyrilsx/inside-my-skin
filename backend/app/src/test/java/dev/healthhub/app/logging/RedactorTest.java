package dev.healthhub.app.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RedactorTest {

    @Test
    void masksJsonCredentialValuesKeepingQuotes() {
        var out = Redactor.apply("{\"clientId\":\"***\",\"clientSecret\":\"super-secret-value\"}");
        assertFalse(out.contains("super-secret-value"), "secret value must be masked: " + out);
        assertTrue(out.contains("\"clientSecret\":\"***\""), out);
        assertFalse(out.contains("\"clientId\":\"abc\""), "client id is masked too: " + out);
    }

    @Test
    void masksQueryStringAndHeaders() {
        var out = Redactor.apply("/connections/withings/callback?code=oauth-code-123&state=xyz789&foo=bar");
        assertFalse(out.contains("oauth-code-123"), out);
        assertFalse(out.contains("xyz789"), out);
        assertTrue(out.contains("foo=bar"), out);
        var header = Redactor.apply("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.token.sig");
        assertFalse(header.contains("eyJhbGciOiJIUzI1NiJ9"), header);
        assertTrue(header.contains("***"), header);
    }

    @Test
    void masksFormEncodedAndKeyValuePairs() {
        var out = Redactor.apply("client_id=abc123&redirect_uri=http://localhost/cb");
        assertFalse(out.contains("abc123"), out);
        assertTrue(out.contains("redirect_uri=http://localhost/cb"), out);
        assertEquals("personalKey: ***", Redactor.apply("personalKey: pk-999"));
    }

    @Test
    void leavesHarmlessTextUntouched() {
        assertEquals("sync completed: connection=123 provider=garmin",
                Redactor.apply("sync completed: connection=123 provider=garmin"));
        assertEquals("", Redactor.apply(""));
    }
}
