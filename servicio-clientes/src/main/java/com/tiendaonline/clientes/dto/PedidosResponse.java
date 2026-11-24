package com.tiendaonline.clientes.dto;

import java.util.List;

public record PedidosResponse(
        List<PedidoResponse> pedidos) {
}
