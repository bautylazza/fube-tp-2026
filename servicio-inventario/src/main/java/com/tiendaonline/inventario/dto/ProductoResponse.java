package com.tiendaonline.inventario.dto;

import java.util.UUID;

public record ProductoResponse(
    UUID id,
    String sku,
    String nombre,
    Integer cantidadDisponible
) {

    public static ProductoResponse fromEntity(com.tiendaonline.inventario.entity.Producto producto) {
        return new ProductoResponse(
            producto.getId(),
            producto.getSku(),
            producto.getNombre(),
            producto.getCantidadDisponible()
        );
    }
}
