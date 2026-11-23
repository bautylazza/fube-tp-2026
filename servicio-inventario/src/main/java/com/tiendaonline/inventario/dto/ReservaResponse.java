package com.tiendaonline.inventario.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.entity.Reserva;

public record ReservaResponse(
    UUID id,
    UUID productoId,
    UUID pedidoId,
    Integer cantidad,
    EstadoReserva estado,
    LocalDateTime fechaCreacion
) {
    public static ReservaResponse fromEntity(Reserva reserva) {
        return new ReservaResponse(
            reserva.getId(),
            reserva.getProductoId(),
            reserva.getPedidoId(),
            reserva.getCantidad(),
            reserva.getEstado(),
            reserva.getFechaCreacion()
        );
    }
}
