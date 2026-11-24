package com.tiendaonline.pedidos.batch;

import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoExpirado(
    UUID id,
    LocalDateTime fechaActualizacion
) {}
