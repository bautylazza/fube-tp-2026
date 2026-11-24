package com.tiendaonline.clientes.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tiendaonline.clientes.dto.PedidosResponse;
import com.tiendaonline.clientes.exception.ServicioNoDisponibleException;

@Component
public class PedidosClient {

    private final RestClient restClient;

    public PedidosClient(@Qualifier("pedidosRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public PedidosResponse listarPorCliente(UUID clienteId) {
        try {
            return restClient.get()
                    .uri("/api/v1/pedidos?clienteId={clienteId}", clienteId)
                    .retrieve()
                    .body(PedidosResponse.class);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("servicio-pedidos no responde", ex);
        }
    }
}
