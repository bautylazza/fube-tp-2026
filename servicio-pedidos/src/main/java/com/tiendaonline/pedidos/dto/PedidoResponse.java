package com.tiendaonline.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.entity.Pedido;

import io.swagger.v3.oas.annotations.media.Schema;

public record PedidoResponse(
    @Schema(example = "0b5f1c9e-3d0a-4b7a-9d55-6d1c1d0c8a11") UUID id,
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID clienteId,
    @Schema(example = "CREADO") EstadoPedido estado,
    @Schema(example = "3001.00") BigDecimal montoTotal,
    @Schema(example = "2026-11-24T10:30:00") LocalDateTime fechaCreacion,
    @Schema(example = "2026-11-24T10:30:00") LocalDateTime fechaActualizacion,
    List<ItemPedidoResponse> items
) {
    public static PedidoResponse fromEntity(Pedido pedido) {
        return new PedidoResponse(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getEstado(),
            pedido.getMontoTotal(),
            pedido.getFechaCreacion(),
            pedido.getFechaActualizacion(),
            pedido.getItems().stream().map(ItemPedidoResponse::fromEntity).toList()
        );
    }
}
