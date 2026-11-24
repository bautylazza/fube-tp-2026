package com.tiendaonline.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ActualizarReservaRequest(
    @NotNull @Pattern (regexp="LIBERADA", message="El estado de la reserva debe ser 'LIBERADA'") String estado
) {

}
