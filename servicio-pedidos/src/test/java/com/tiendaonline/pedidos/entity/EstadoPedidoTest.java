package com.tiendaonline.pedidos.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EstadoPedidoTest {

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = { "CREADO", "CONFIRMADO" })
    void sePuedeCancelarDesdeCreadoOConfirmado(EstadoPedido estado) {
        assertThat(estado.puedePasarA(EstadoPedido.CANCELADO)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = { "CREADO", "CONFIRMADO" }, mode = EnumSource.Mode.EXCLUDE)
    void noSePuedeCancelarDesdeOtroEstado(EstadoPedido estado) {
        assertThat(estado.puedePasarA(EstadoPedido.CANCELADO)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(EstadoPedido.class)
    void soloSePuedeConfirmarDesdeCreado(EstadoPedido estado) {
        assertThat(estado.puedePasarA(EstadoPedido.CONFIRMADO)).isEqualTo(estado == EstadoPedido.CREADO);
    }

    @ParameterizedTest
    @EnumSource(EstadoPedido.class)
    void soloSePuedeExpirarDesdeCreado(EstadoPedido estado) {
        assertThat(estado.puedePasarA(EstadoPedido.EXPIRADO)).isEqualTo(estado == EstadoPedido.CREADO);
    }

    @ParameterizedTest
    @EnumSource(EstadoPedido.class)
    void nuncaSePuedeVolverACreado(EstadoPedido estado) {
        assertThat(estado.puedePasarA(EstadoPedido.CREADO)).isFalse();
    }
}
