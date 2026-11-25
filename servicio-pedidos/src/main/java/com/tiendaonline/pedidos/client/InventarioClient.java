package com.tiendaonline.pedidos.client;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;
import com.tiendaonline.pedidos.exception.StockInsuficienteException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InventarioClient {

    private final RestClient restClient;

    public InventarioClient(@Qualifier("inventarioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void reservar(UUID pedidoId, UUID productoId, Integer cantidad) {
        try {
            restClient.post()
                    .uri("/api/v1/reservas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReservarStockRequest(productoId, pedidoId, cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict ex) {
            log.warn("Stock insuficiente {} {}", kv("pedidoId", pedidoId), kv("productoId", productoId));
            throw new StockInsuficienteException("Stock insuficiente para el producto: " + productoId);
        } catch (HttpClientErrorException.UnprocessableEntity ex) {
            log.warn("Producto inexistente {} {}", kv("pedidoId", pedidoId), kv("productoId", productoId));
            throw new ReferenciaInvalidaException("Producto no encontrado: " + productoId);
        } catch (RestClientException ex) {
            log.error("servicio-inventario no responde al reservar {} {}", kv("pedidoId", pedidoId),
                    kv("productoId", productoId), ex);
            throw new ServicioNoDisponibleException("servicio-inventario no responde", ex);
        }
    }

    public void liberarPorPedido(UUID pedidoId) {
        try {
            restClient.patch()
                    .uri("/api/v1/reservas?pedidoId={pedidoId}", pedidoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LiberarReservaRequest("LIBERADA"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("servicio-inventario no responde al liberar {}", kv("pedidoId", pedidoId), ex);
            throw new ServicioNoDisponibleException("servicio-inventario no responde", ex);
        }
    }
}
