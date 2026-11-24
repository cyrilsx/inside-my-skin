package dev.healthhub.app.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger handlerLogger;

    @BeforeEach
    void setUp() {
        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        handlerLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        handlerLogger.detachAppender(appender);
    }

    @Test
    void unexpectedErrorBecomes500ProblemAndLogIsRedacted() {
        var handler = new GlobalExceptionHandler();
        var request = new MockHttpServletRequest("PUT", "/api/v1/providers/withings/credentials");

        var response = handler.handle(new IllegalStateException("save failed: clientSecret=secret-token-123"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("application/problem+json", response.getHeaders().getContentType().toString());

        var logged = appender.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + "\n" + b);
        assertTrue(logged.contains("unhandled error"), logged);
        assertFalse(logged.contains("secret-token-123"), "secret in exception message must be redacted: " + logged);
    }
}
