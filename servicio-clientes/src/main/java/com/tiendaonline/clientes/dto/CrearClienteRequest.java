package com.tiendaonline.clientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(
        @Schema(example = "Juan Perez") @NotBlank @Size(max = 255) String nombre,
        @Schema(example = "juan@gmail.com") @NotBlank @Email @Size(max = 255) String email) {
}
