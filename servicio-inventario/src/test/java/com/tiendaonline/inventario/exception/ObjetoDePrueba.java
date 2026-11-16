package com.tiendaonline.inventario.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * ObjetoDePrueba to GlobalExceptionHandlerTest.
 */
record ObjetoDePrueba(@NotBlank String nombre, @Positive Integer cantidad) {

}
