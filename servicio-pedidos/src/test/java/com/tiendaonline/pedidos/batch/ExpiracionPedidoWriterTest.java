package com.tiendaonline.pedidos.batch;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class ExpiracionPedidoWriterTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private ExpiracionPedidoWriter writer;

    private final PedidoExpirado pedido = new PedidoExpirado(UUID.randomUUID(), LocalDateTime.now());

    @Test
    void siElPedidoSeExpiroLiberaSusReservas() {
        when(pedidoRepository.expirarSiSigueCreado(pedido.id(), pedido.fechaActualizacion())).thenReturn(1);

        writer.write(Chunk.of(pedido));

        verify(inventarioClient).liberarPorPedido(pedido.id());
    }

    @Test
    void siElPedidoYaNoEstaCreadoNoLiberaSusReservas() {
        when(pedidoRepository.expirarSiSigueCreado(pedido.id(), pedido.fechaActualizacion())).thenReturn(0);

        writer.write(Chunk.of(pedido));

        verifyNoInteractions(inventarioClient);
    }
}
