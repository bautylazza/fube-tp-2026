package com.tiendaonline.clientes.config;

import java.io.IOException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TraceIdResponseFilter extends OncePerRequestFilter {

    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    private final ObjectProvider<Tracer> tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Tracer actual = tracer.getIfAvailable();
        Span span = actual != null ? actual.currentSpan() : null;
        if (span != null) {
            response.setHeader(HEADER_TRACE_ID, span.context().traceId());
        }
        chain.doFilter(request, response);
    }
}
