package com.tiendaonline.pedidos.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.net.http.HttpRequest;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;

@SpringBootTest 
public class ClientesClientTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private ClientesClientTest clientesClient;

    private final UUID clienteId = UUID.randomUUID();

    @DynamicPropertySource 
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.clientes.url", () -> "http://localhost:" + mockServer.getLocalPort());
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
    void clienteExistenteNoLanzaExcepcion() {
        mockServer.when(pedirCliente()).respond(response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\"id\":\"" + clienteId + "\",\"nombre\":\"Juan Perez\",\"email\":\"juan@gmail.com\"}"));

        assertThatCode(() -> clientesClient.verificarExiste(clienteId)).doesNotThrowAnyException();

        mockServer.verify(pedirCliente());
    }

    @Test
    void clienteInexistenteLanzaReferenciaInvalida() {
        mockServer.when(pedirCliente()).respond(response().withStatusCode(404));

        assertThatThrownBy(() -> clientesClient.verificarExiste(clienteId))
                .isInstanceOf(ReferenciaInvalidaException.class)
                .hasMessageContaining(clienteId.toString());
    }

    @Test
    void errorDelServidorLanzaServicioNoDisponible() {
        mockServer.when(pedirCliente()).respond(response().withStatusCode(500));

        assertThatThrownBy(() -> clientesClient.verificarExiste(clienteId))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }

    private HttpRequest pedirCliente() {
        return request().withMethod("GET").withPath("/api/v1/clientes/" + clienteId);
    }
}
