package com.tiendaonline.clientes.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record ItemPedidoResponse(
        @Schema(example = "7c9e6679-7425-40de-944b-e07fc1f90ae7") UUID id,
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID productoId,
        @Schema(example = "2") Integer cantidad,
        @Schema(example = "1500.50") BigDecimal precioUnitario) {
}
