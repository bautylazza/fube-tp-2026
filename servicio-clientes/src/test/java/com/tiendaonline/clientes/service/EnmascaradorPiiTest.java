package com.tiendaonline.clientes.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EnmascaradorPiiTest {

    private final EnmascaradorPii enmascarador = new EnmascaradorPii();

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "Juan Perez        | J*** P***",
        "Juan              | J***",
        "Juan  Perez       | J*** P***",
        "' Juan Perez '    | J*** P***",
        "Maria Jose Lopez  | M*** J*** L***"
    })
    void enmascaraCadaPalabraDelNombre(String nombre, String esperado) {
        assertThat(enmascarador.nombre(nombre)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "juan@gmail.com    | ****@****.com",
        "a@mail.com.ar     | ****@****.ar",
        "juan.perez@uni.edu| ****@****.edu",
        "juan@localhost    | ****@****"
    })
    void enmascaraElEmailDejandoLaTerminacion(String email, String esperado) {
        assertThat(enmascarador.email(email)).isEqualTo(esperado);
    }

    @Test
    void valoresNulosQuedanNulos() {
        assertThat(enmascarador.nombre(null)).isNull();
        assertThat(enmascarador.email(null)).isNull();
    }
}
