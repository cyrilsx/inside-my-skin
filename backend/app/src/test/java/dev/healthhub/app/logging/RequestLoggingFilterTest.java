package dev.healthhub.app.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger filterLogger;

    @BeforeEach
    void setUp() {
        filterLogger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        filterLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        filterLogger.detachAppender(appender);
    }

    @Test
    void logsRequestLineWithRedactedQueryButNeverBodyOrSecret() throws Exception {
        var filter = new RequestLoggingFilter();
        var request = new MockHttpServletRequest("PUT", "/api/v1/providers/withings/credentials");
        request.setQueryString("clientId=abc&clientSecret=top-secret-value");
        request.setContent("{\"credentials\":{\"clientSecret\":\"top-secret-value\"}}".getBytes());
        request.setContentType("application/json");
        var response = new MockHttpServletResponse();
        response.setStatus(204);

        filter.doFilter(request, response, new MockFilterChain());

        var logged = appender.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + "\n" + b);
        assertTrue(logged.contains("PUT"), logged);
        assertTrue(logged.contains("/api/v1/providers/withings/credentials"), logged);
        assertTrue(logged.contains("204"), logged);
        assertTrue(logged.contains("clientId=***"), "client id is masked too: " + logged);
        assertFalse(logged.contains("top-secret-value"), "secret must never be logged: " + logged);
    }

    @Test
    void logsServerErrorsAtErrorLevel() throws Exception {
        var filter = new RequestLoggingFilter();
        var request = new MockHttpServletRequest("GET", "/api/v1/dailies");
        var response = new MockHttpServletResponse();
        response.setStatus(500);

        filter.doFilter(request, response, new MockFilterChain());

        assertTrue(appender.list.stream().anyMatch(e -> e.getLevel().toString().equals("ERROR")));
    }
}
