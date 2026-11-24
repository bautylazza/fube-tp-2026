package com.tiendaonline.inventario.dto;

import java.util.UUID;

public record DisponibilidadResponse(
    UUID productoId,
    Integer cantidadDisponible,
    Boolean disponible
) {

}
