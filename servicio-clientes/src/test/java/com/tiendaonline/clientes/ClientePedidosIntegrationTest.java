package com.tiendaonline.clientes;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.clientes.entity.Cliente;
import com.tiendaonline.clientes.repository.ClienteRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientePedidosIntegrationTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    private UUID clienteId;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.pedidos.url", () -> "http://localhost:" + mockServer.getLocalPort());
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
        Cliente cliente = new Cliente(null, "Ana Gomez", "ana.pedidos@mail.com", LocalDateTime.now());
        clienteId = clienteRepository.save(cliente).getId();
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }

    @Test
    void devuelveLosPedidosQueRespondeServicioPedidos() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        mockServer.when(pedirPedidos()).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("""
                        {"pedidos":[{"id":"%s","clienteId":"%s","estado":"CONFIRMADO","montoTotal":250.00,
                        "fechaCreacion":"2026-11-24T10:30:00","fechaActualizacion":"2026-11-24T11:00:00","items":[]}]}
                        """.formatted(pedidoId, clienteId)));

        mockMvc.perform(get("/api/v1/clientes/{id}/pedidos", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidos.length()").value(1))
                .andExpect(jsonPath("$.pedidos[0].id").value(pedidoId.toString()))
                .andExpect(jsonPath("$.pedidos[0].estado").value("CONFIRMADO"));
    }

    @Test
    void clienteSinPedidosDevuelveListaVacia() throws Exception {
        mockServer.when(pedirPedidos()).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\"pedidos\":[]}"));

        mockMvc.perform(get("/api/v1/clientes/{id}/pedidos", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidos").isEmpty());
    }

    @Test
    void siServicioPedidosFallaDevuelve503() throws Exception {
        mockServer.when(pedirPedidos()).respond(response().withStatusCode(500));

        mockMvc.perform(get("/api/v1/clientes/{id}/pedidos", clienteId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Servicio no disponible"));
    }

    private HttpRequest pedirPedidos() {
        return request().withMethod("GET").withPath("/api/v1/pedidos")
                .withQueryStringParameter("clienteId", clienteId.toString());
    }
}
