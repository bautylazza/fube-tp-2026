package com.tiendaonline.pedidos.dto;

import java.util.List;

public record PedidosResponse(
    List<PedidoResponse> pedidos
) {}
