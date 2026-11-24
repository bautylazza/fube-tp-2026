package com.tiendaonline.pedidos.client;

import java.util.UUID;

public record ReservarStockRequest(
    UUID productoId,
    UUID pedidoId,
    Integer cantidad
) {}
