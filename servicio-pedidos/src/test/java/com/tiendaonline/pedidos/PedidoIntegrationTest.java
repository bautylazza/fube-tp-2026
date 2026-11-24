package com.tiendaonline.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.entity.Pedido;
import com.tiendaonline.pedidos.repository.PedidoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoIntegrationTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID producto1 = UUID.randomUUID();
    private final UUID producto2 = UUID.randomUUID();

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
        mockServer.when(reservaDe(producto1)).respond(response().withStatusCode(201));
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
    void crearPedidoReservaElStockYLoGuarda() throws Exception {
        mockServer.when(reservaDe(producto2)).respond(response().withStatusCode(201));

        String body = crearPedido(201);

        Pedido guardado = pedidoRepository.findByClienteId(clienteId).get(0);
        assertThat(guardado.getId()).isEqualTo(UUID.fromString(JsonPath.read(body, "$.id")));
        assertThat(guardado.getEstado()).isEqualTo(EstadoPedido.CREADO);
        assertThat(guardado.getMontoTotal()).isEqualByComparingTo("251.00");
        assertThat(guardado.getItems()).hasSize(2);
        mockServer.verify(reservaDe(producto1), VerificationTimes.once());
        mockServer.verify(reservaDe(producto2), VerificationTimes.once());
    }

    @Test
    void faltaDeStockEnElSegundoItemLiberaLaReservaYNoGuardaElPedido() throws Exception {
        mockServer.when(reservaDe(producto2)).respond(response().withStatusCode(409));
        mockServer.when(request().withMethod("PATCH").withPath("/api/v1/reservas"))
                .respond(response().withStatusCode(200));

        crearPedido(409);

        assertThat(pedidoRepository.findByClienteId(clienteId)).isEmpty();
        mockServer.verify(request().withMethod("PATCH").withPath("/api/v1/reservas")
                .withQueryStringParameter("pedidoId", pedidoIdReservado())
                .withBody(json("{\"estado\":\"LIBERADA\"}")), VerificationTimes.once());
    }

    private String crearPedido(int estadoEsperado) throws Exception {
        return mockMvc.perform(post("/api/v1/pedidos")
                .contentType("application/json")
                .content("{\"clienteId\":\"" + clienteId + "\",\"items\":["
                        + "{\"productoId\":\"" + producto1 + "\",\"cantidad\":2,\"precioUnitario\":100.50},"
                        + "{\"productoId\":\"" + producto2 + "\",\"cantidad\":1,\"precioUnitario\":50.00}]}"))
                .andExpect(status().is(estadoEsperado))
                .andReturn().getResponse().getContentAsString();
    }

    private HttpRequest reservaDe(UUID productoId) {
        return request().withMethod("POST").withPath("/api/v1/reservas")
                .withBody(json("{\"productoId\":\"" + productoId + "\"}"));
    }

    private String pedidoIdReservado() {
        HttpRequest[] reservas = mockServer.retrieveRecordedRequests(reservaDe(producto1));
        assertThat(List.of(reservas)).hasSize(1);
        return JsonPath.read(reservas[0].getBodyAsString(), "$.pedidoId");
    }
}
