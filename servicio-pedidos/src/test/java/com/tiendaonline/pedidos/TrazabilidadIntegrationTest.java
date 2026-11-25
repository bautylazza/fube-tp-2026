package com.tiendaonline.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.tiendaonline.pedidos.repository.PedidoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability(metrics = false)
class TrazabilidadIntegrationTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.clientes.url", () -> "http://localhost:" + mockServer.getLocalPort());
        registry.add("servicios.inventario.url", () -> "http://localhost:" + mockServer.getLocalPort());
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
        mockServer.when(request().withMethod("GET").withPath("/api/v1/clientes/" + clienteId))
                .respond(response().withStatusCode(200));
        mockServer.when(request().withMethod("POST").withPath("/api/v1/reservas"))
                .respond(response().withStatusCode(201));
    }

    @AfterEach
    void borrarPedidos() {
        pedidoRepository.deleteAll(pedidoRepository.findByClienteId(clienteId));
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }

    @Test
    void crearPedidoDevuelveXTraceIdYPropagaElMismoTraceAClientesEInventario() throws Exception {
        String traceId = mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"%s","items":[{"productoId":"%s","cantidad":1,"precioUnitario":10.00}]}
                                """.formatted(clienteId, productoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("X-Trace-Id");

        assertThat(traceId).isNotBlank();
        String traceparent = "00-" + traceId + "-.*";
        mockServer.verify(request().withMethod("GET").withPath("/api/v1/clientes/" + clienteId)
                .withHeader("traceparent", traceparent), VerificationTimes.once());
        mockServer.verify(request().withMethod("POST").withPath("/api/v1/reservas")
                .withHeader("traceparent", traceparent), VerificationTimes.once());
    }
}
