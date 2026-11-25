package com.tiendaonline.pedidos.client;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component 
public class ClientesClient {

    private final RestClient restClient;

    public ClientesClient(@Qualifier("clientesRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void verificarExiste(UUID clienteId) {
        try {
            restClient.get()
                    .uri("/api/v1/clientes/{id}", clienteId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Cliente no encontrado {}", kv("clienteId", clienteId));
            throw new ReferenciaInvalidaException("Cliente no encontrado: " + clienteId);
        } catch (RestClientException ex) {
            log.error("servicio-clientes no responde {}", kv("clienteId", clienteId), ex);
            throw new ServicioNoDisponibleException("servicio-clientes no responde", ex);
        }
    }
}
