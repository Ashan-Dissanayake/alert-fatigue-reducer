package io.github.ashan.alertfatiguereducer.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that establishes and propagates a Correlation ID across the request lifecycle.
 * Populates SLF4J MDC context for structured logging traceability.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_CORRELATION_ID_KEY = "correlationId";
    public static final String MDC_HTTP_METHOD_KEY = "httpMethod";
    public static final String MDC_REQUEST_URI_KEY = "requestUri";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = resolveCorrelationId(request);

        MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
        MDC.put(MDC_HTTP_METHOD_KEY, request.getMethod());
        MDC.put(MDC_REQUEST_URI_KEY, request.getRequestURI());

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID_KEY);
            MDC.remove(MDC_HTTP_METHOD_KEY);
            MDC.remove(MDC_REQUEST_URI_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (StringUtils.hasText(correlationId)) {
            return correlationId.trim();
        }

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.hasText(requestId)) {
            return requestId.trim();
        }

        return UUID.randomUUID().toString();
    }
}
