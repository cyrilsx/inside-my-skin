package dev.healthhub.app.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs one line per HTTP request: method, path (query string redacted), status, duration, request id.
 * Request/response bodies are never logged — credential payloads and tokens only ever travel in bodies
 * or the Authorization header, and neither is logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var requestId = UUID.randomUUID().toString();
        MDC.put(MDC_REQUEST_ID, requestId);
        var started = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("request {} {} failed: {}", request.getMethod(), pathOf(request), Redactor.apply(e.getMessage()), e);
            throw e;
        } finally {
            var durationMs = (System.nanoTime() - started) / 1_000_000;
            var level = response.getStatus() >= 500 ? "error" : response.getStatus() >= 400 ? "warn" : "info";
            var message = String.format("http %s %s -> %d (%d ms)", request.getMethod(), pathOf(request),
                    response.getStatus(), durationMs);
            switch (level) {
                case "error" -> log.error(message);
                case "warn" -> log.warn(message);
                default -> log.info(message);
            }
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private static String pathOf(HttpServletRequest request) {
        var query = request.getQueryString();
        return query == null ? request.getRequestURI() : Redactor.apply(request.getRequestURI() + "?" + query);
    }
}
