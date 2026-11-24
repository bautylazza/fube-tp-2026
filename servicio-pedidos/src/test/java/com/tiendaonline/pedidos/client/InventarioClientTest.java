package com.tiendaonline.pedidos.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;
import com.tiendaonline.pedidos.exception.StockInsuficienteException;

@SpringBootTest
class InventarioClientTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private InventarioClient inventarioClient;

    private final UUID pedidoId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.inventario.url", () -> "http://localhost:" + mockServer.getLocalPort());
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }

    @Test
    void reservarEnviaPedidoProductoYCantidad() {
        mockServer.when(pedidoDeReserva()).respond(response().withStatusCode(201));

        inventarioClient.reservar(pedidoId, productoId, 2);

        mockServer.verify(pedidoDeReserva().withBody(json(
                "{\"pedidoId\":\"" + pedidoId + "\",\"productoId\":\"" + productoId + "\",\"cantidad\":2}")));
    }

    @Test
    void reservarSinStockLanzaStockInsuficiente() {
        mockServer.when(pedidoDeReserva()).respond(response().withStatusCode(409));

        assertThatThrownBy(() -> inventarioClient.reservar(pedidoId, productoId, 2))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining(productoId.toString());
    }

    @Test
    void reservarProductoInexistenteLanzaReferenciaInvalida() {
        mockServer.when(pedidoDeReserva()).respond(response().withStatusCode(422));

        assertThatThrownBy(() -> inventarioClient.reservar(pedidoId, productoId, 2))
                .isInstanceOf(ReferenciaInvalidaException.class)
                .hasMessageContaining(productoId.toString());
    }

    @Test
    void reservarConErrorDelServidorLanzaServicioNoDisponible() {
        mockServer.when(pedidoDeReserva()).respond(response().withStatusCode(500));

        assertThatThrownBy(() -> inventarioClient.reservar(pedidoId, productoId, 2))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }

    @Test
    void liberarPorPedidoEnviaEstadoLiberada() {
        mockServer.when(pedidoDeLiberacion()).respond(response().withStatusCode(200));

        inventarioClient.liberarPorPedido(pedidoId);

        mockServer.verify(pedidoDeLiberacion().withBody(json("{\"estado\":\"LIBERADA\"}")));
    }

    @Test
    void liberarConErrorDelServidorLanzaServicioNoDisponible() {
        mockServer.when(pedidoDeLiberacion()).respond(response().withStatusCode(500));

        assertThatThrownBy(() -> inventarioClient.liberarPorPedido(pedidoId))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }

    private HttpRequest pedidoDeReserva() {
        return request().withMethod("POST").withPath("/api/v1/reservas");
    }

    private HttpRequest pedidoDeLiberacion() {
        return request().withMethod("PATCH").withPath("/api/v1/reservas")
                .withQueryStringParameter("pedidoId", pedidoId.toString());
    }
}
