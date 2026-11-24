package com.tiendaonline.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ActualizarPedidoRequest(
    @Schema(example = "CANCELADO")
    @NotNull
    @Pattern(regexp = "CANCELADO|CONFIRMADO", message = "El estado del pedido debe ser 'CANCELADO' o 'CONFIRMADO'")
    String estado
) {}
