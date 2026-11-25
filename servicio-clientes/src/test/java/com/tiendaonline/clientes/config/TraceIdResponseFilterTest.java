package com.tiendaonline.clientes.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.micrometer.tracing.Tracer;

class TraceIdResponseFilterTest {

    private final Tracer tracer = mock(Tracer.class, RETURNS_DEEP_STUBS);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<Tracer> proveedor = mock(ObjectProvider.class);
    private final TraceIdResponseFilter filtro = new TraceIdResponseFilter(proveedor);

    @Test
    void devuelveElTraceIdDeLaRequestEnElHeaderXTraceId() throws Exception {
        when(proveedor.getIfAvailable()).thenReturn(tracer);
        when(tracer.currentSpan().context().traceId()).thenReturn("6560a3b1e2f4c9d07a1b2c3d4e5f6071");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("6560a3b1e2f4c9d07a1b2c3d4e5f6071");
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void sinSpanActivoNoAgregaElHeaderYSigueLaCadena() throws Exception {
        when(proveedor.getIfAvailable()).thenReturn(tracer);
        when(tracer.currentSpan()).thenReturn(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(response.getHeader("X-Trace-Id")).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void sinTracingConfiguradoNoAgregaElHeader() throws Exception {
        when(proveedor.getIfAvailable()).thenReturn(null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader("X-Trace-Id")).isNull();
    }
}
