package com.tiendaonline.pedidos.batch;

import java.util.UUID;

import com.tiendaonline.pedidos.entity.EstadoPedido;

public record PedidoVencido(
    UUID id,
    EstadoPedido estado
) {}
