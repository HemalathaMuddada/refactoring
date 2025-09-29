package com.xtremand.auth.config;

import java.io.IOException;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.xtremand.common.util.TraceIdGenerator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_HEADER = "X-Request-ID";
    private static final String TRACE_HEADER = "X-Trace-ID";
    private static final String REQUEST_MDC_KEY = "requestId";
    private static final String TRACE_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        String traceId = TraceIdGenerator.get();

        response.setHeader(REQUEST_HEADER, requestId);
        response.setHeader(TRACE_HEADER, traceId);

        MDC.put(REQUEST_MDC_KEY, requestId);
        MDC.put(TRACE_MDC_KEY, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_MDC_KEY);
            MDC.remove(TRACE_MDC_KEY);
        }
    }
}
