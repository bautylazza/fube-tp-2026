package com.tiendaonline.clientes.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tiendaonline.clientes.dto.PedidoResponse;
import com.tiendaonline.clientes.dto.PedidosResponse;
import com.tiendaonline.clientes.exception.ServicioNoDisponibleException;

@SpringBootTest
class PedidosClientTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private PedidosClient pedidosClient;

    private final UUID clienteId = UUID.randomUUID();

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.pedidos.url", () -> "http://localhost:" + mockServer.getLocalPort());
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
    void devuelveLosPedidosDelCliente() {
        UUID pedidoId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        mockServer.when(pedirPedidos()).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("""
                        {"pedidos":[{"id":"%s","clienteId":"%s","estado":"CREADO","montoTotal":3001.00,
                        "fechaCreacion":"2026-11-24T10:30:00","fechaActualizacion":"2026-11-24T10:30:00",
                        "items":[{"id":"%s","productoId":"%s","cantidad":2,"precioUnitario":1500.50}]}]}
                        """.formatted(pedidoId, clienteId, UUID.randomUUID(), productoId)));

        PedidosResponse respuesta = pedidosClient.listarPorCliente(clienteId);

        assertThat(respuesta.pedidos()).hasSize(1);
        PedidoResponse pedido = respuesta.pedidos().get(0);
        assertThat(pedido.id()).isEqualTo(pedidoId);
        assertThat(pedido.estado()).isEqualTo("CREADO");
        assertThat(pedido.montoTotal()).isEqualByComparingTo("3001.00");
        assertThat(pedido.items()).singleElement()
                .satisfies(item -> assertThat(item.productoId()).isEqualTo(productoId));
        mockServer.verify(pedirPedidos());
    }

    @Test
    void clienteSinPedidosDevuelveListaVacia() {
        mockServer.when(pedirPedidos()).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\"pedidos\":[]}"));

        assertThat(pedidosClient.listarPorCliente(clienteId).pedidos()).isEmpty();
    }

    @Test
    void errorDelServidorLanzaServicioNoDisponible() {
        mockServer.when(pedirPedidos()).respond(response().withStatusCode(500));

        assertThatThrownBy(() -> pedidosClient.listarPorCliente(clienteId))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }

    private HttpRequest pedirPedidos() {
        return request().withMethod("GET").withPath("/api/v1/pedidos")
                .withQueryStringParameter("clienteId", clienteId.toString());
    }
}
