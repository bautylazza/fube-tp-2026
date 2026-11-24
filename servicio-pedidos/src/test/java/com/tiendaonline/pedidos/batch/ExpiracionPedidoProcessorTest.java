package com.tiendaonline.pedidos.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.tiendaonline.pedidos.entity.EstadoPedido;

class ExpiracionPedidoProcessorTest {

    private final ExpiracionPedidoProcessor processor = new ExpiracionPedidoProcessor();
    private final UUID pedidoId = UUID.randomUUID();

    @Test
    void unPedidoCreadoSeExpiraConFechaActual() {
        PedidoExpirado resultado = processor.process(new PedidoVencido(pedidoId, EstadoPedido.CREADO));

        assertThat(resultado.id()).isEqualTo(pedidoId);
        assertThat(resultado.fechaActualizacion()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = "CREADO", mode = EnumSource.Mode.EXCLUDE)
    void unPedidoEnOtroEstadoSeFiltra(EstadoPedido estado) {
        assertThat(processor.process(new PedidoVencido(pedidoId, estado))).isNull();
    }
}
