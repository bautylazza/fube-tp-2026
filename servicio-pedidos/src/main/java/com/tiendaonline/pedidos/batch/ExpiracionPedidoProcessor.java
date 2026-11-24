package com.tiendaonline.pedidos.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.tiendaonline.pedidos.entity.EstadoPedido;

@Component
public class ExpiracionPedidoProcessor implements ItemProcessor<PedidoVencido, PedidoExpirado> {

    @Override
    public PedidoExpirado process(PedidoVencido pedido) {
        if (!pedido.estado().puedePasarA(EstadoPedido.EXPIRADO)) {
            return null;
        }
        return new PedidoExpirado(pedido.id(), LocalDateTime.now());
    }
}
