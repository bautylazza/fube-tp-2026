package com.tiendaonline.inventario.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservaRequest(
    @NotNull UUID productoId,
    @NotNull UUID pedidoId,
    @NotNull @Positive Integer cantidad
) {}
