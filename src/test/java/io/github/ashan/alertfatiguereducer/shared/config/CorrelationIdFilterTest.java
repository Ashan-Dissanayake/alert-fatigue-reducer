package io.github.ashan.alertfatiguereducer.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate new correlation ID and set response header when header is missing")
    void shouldGenerateNewCorrelationIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/alerts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcCorrelationIdDuringChain = new AtomicReference<>();
        AtomicReference<String> mdcMethodDuringChain = new AtomicReference<>();
        AtomicReference<String> mdcUriDuringChain = new AtomicReference<>();

        FilterChain filterChain = (req, res) -> {
            mdcCorrelationIdDuringChain.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));
            mdcMethodDuringChain.set(MDC.get(CorrelationIdFilter.MDC_HTTP_METHOD_KEY));
            mdcUriDuringChain.set(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI_KEY));
        };

        filter.doFilter(request, response, filterChain);

        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(responseHeader).isNotBlank();
        assertThat(mdcCorrelationIdDuringChain.get()).isEqualTo(responseHeader);
        assertThat(mdcMethodDuringChain.get()).isEqualTo("POST");
        assertThat(mdcUriDuringChain.get()).isEqualTo("/api/v1/alerts");

        // MDC must be cleared after filter execution
        assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_HTTP_METHOD_KEY)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI_KEY)).isNull();
    }

    @Test
    @DisplayName("Should propagate existing X-Correlation-ID header to MDC and response")
    void shouldPropagateExistingCorrelationIdWhenHeaderProvided() throws ServletException, IOException {
        String existingCorrelationId = "corr-fixed-12345";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/incidents");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingCorrelationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcCorrelationIdDuringChain = new AtomicReference<>();

        FilterChain filterChain = (req, res) ->
                mdcCorrelationIdDuringChain.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(existingCorrelationId);
        assertThat(mdcCorrelationIdDuringChain.get()).isEqualTo(existingCorrelationId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should fallback to X-Request-ID header when X-Correlation-ID is missing")
    void shouldFallbackToRequestIdWhenCorrelationIdMissing() throws ServletException, IOException {
        String requestId = "req-fixed-99999";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/alerts");
        request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, requestId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcCorrelationIdDuringChain = new AtomicReference<>();

        FilterChain filterChain = (req, res) ->
                mdcCorrelationIdDuringChain.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(requestId);
        assertThat(mdcCorrelationIdDuringChain.get()).isEqualTo(requestId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should clean up MDC even if downstream filter chain throws exception")
    void shouldCleanUpMdcEvenWhenFilterChainThrowsException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/alerts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            throw new RuntimeException("Downstream error simulation");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Downstream error simulation");

        assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_HTTP_METHOD_KEY)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI_KEY)).isNull();
    }
}
