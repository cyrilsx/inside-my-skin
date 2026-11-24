package dev.healthhub.app.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Last-resort handler: logs the failure (message redacted — exception messages can embed request
 * data) and returns an RFC 9457 problem. Known {@link ErrorResponse} exceptions keep their own
 * status/detail; only truly unexpected errors become 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handle(Exception e, HttpServletRequest request) {
        if (e instanceof ErrorResponse errorResponse) {
            var problem = errorResponse.getBody();
            log.warn("handled error on {} {}: {} {}", request.getMethod(), Redactor.apply(request.getRequestURI()),
                    problem.getStatus(), Redactor.apply(String.valueOf(problem.getDetail())));
            return ResponseEntity.status(problem.getStatus())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(problem);
        }
        log.error("unhandled error on {} {}", request.getMethod(), Redactor.apply(request.getRequestURI()), e);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        problem.setTitle("Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
