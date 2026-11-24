package com.tiendaonline.pedidos.dto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CrearPedidoRequest(
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @NotNull UUID clienteId,
    @NotEmpty List<@NotNull @Valid ItemPedidoRequest> items
) {

    // Limite de la columna monto_total NUMERIC(12, 2)
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("9999999999.99");

    @JsonIgnore
    @AssertTrue(message = "no puede haber productos repetidos")
    public boolean isProductosSinRepetir() {
        if (items == null) {
            return true;
        }
        Set<UUID> vistos = new HashSet<>();
        return items.stream()
                .filter(Objects::nonNull)
                .map(ItemPedidoRequest::productoId)
                .filter(Objects::nonNull)
                .allMatch(vistos::add);
    }

    @JsonIgnore
    @AssertTrue(message = "el monto total del pedido supera el maximo permitido")
    public boolean isMontoTotalValido() {
        if (items == null) {
            return true;
        }
        return items.stream()
                .filter(item -> item != null && item.precioUnitario() != null && item.cantidad() != null)
                .map(item -> item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(MONTO_MAXIMO) <= 0;
    }
}
