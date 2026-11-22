package com.tiendaonline.clientes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tiendaonline.clientes.entity.Cliente;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClienteResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
        @Schema(example = "Juan Perez") String nombre,
        @Schema(example = "juan@gmail.com") String email,
        @Schema(example = "2026-11-17T10:30:00") LocalDateTime fechaAlta) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNombre(), cliente.getEmail(), cliente.getFechaAlta());
    }
}
