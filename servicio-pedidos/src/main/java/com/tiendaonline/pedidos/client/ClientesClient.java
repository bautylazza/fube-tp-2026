package com.tiendaonline.pedidos.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;

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
            throw new ReferenciaInvalidaException("Cliente no encontrado: " + clienteId);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("servicio-clientes no responde", ex);
        }
    }
}
