package com.tiendaonline.pedidos.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequest(
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @NotNull UUID productoId,
    @Schema(example = "2") @NotNull @Positive Integer cantidad,
    @Schema(example = "1500.50") @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario
) {}
