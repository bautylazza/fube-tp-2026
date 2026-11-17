package com.tiendaonline.clientes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tiendaonline.clientes.entity.Cliente;

public record ClienteResponse(UUID id, String nombre, String email, LocalDateTime fechaAlta) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNombre(), cliente.getEmail(), cliente.getFechaAlta());
    }
}
