package com.tiendaonline.pedidos.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExpiracionPedidoWriter implements ItemWriter<PedidoExpirado> {

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;

    @Override
    public void write(Chunk<? extends PedidoExpirado> pedidos) {
        for (PedidoExpirado pedido : pedidos) {
            // Si entre la lectura y la escritura se confirmo o cancelo, el update no
            // afecta filas y su stock no se toca.
            int actualizados = pedidoRepository.expirarSiSigueCreado(pedido.id(), pedido.fechaActualizacion());
            if (actualizados == 1) {
                inventarioClient.liberarPorPedido(pedido.id());
            }
        }
    }
}
