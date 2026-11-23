package com.tiendaonline.inventario.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.inventario.dto.ReservaRequest;
import com.tiendaonline.inventario.dto.ReservasResponse;
import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.entity.Reserva;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;
import com.tiendaonline.inventario.exception.RecursoNoEncontradoException;
import com.tiendaonline.inventario.exception.ReferenciaInvalidaException;
import com.tiendaonline.inventario.exception.StockInsuficienteException;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock ProductoRepository productoRepository;
    @Mock ReservaRepository reservaRepository;
    @InjectMocks ReservaService reservaService;

    private final UUID productoId = UUID.randomUUID();
    private final UUID pedidoId = UUID.randomUUID();


    @Test
    void reservarConProductoSuficienteYReservaExistente() {
        Producto producto = new Producto(productoId, "SKU123", "Producto Test", 100);
        when(productoRepository.findByIdParaActualizar(productoId)).thenReturn(Optional.of(producto));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArguments()[0]);

        ReservaResponse response = reservaService.reservar(new ReservaRequest(productoId, pedidoId, 2));

        assertThat(producto.getCantidadDisponible()).isEqualTo(98);
        assertThat(response.estado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void reservarSinProductoDisponibleConConflictoYNoGuarda() {
        Producto producto = new Producto(productoId, "SKU123", "Producto Test", 1);
        when(productoRepository.findByIdParaActualizar(productoId)).thenReturn(Optional.of(producto));

        ReservaRequest request = new ReservaRequest(productoId, pedidoId, 2);

        assertThatThrownBy(() -> reservaService.reservar(request))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("No hay suficiente cantidad disponible para el producto");
        
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void reservarProductoInexistenteLanzaReferenciaInvalida() {
        when(productoRepository.findByIdParaActualizar(productoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.reservar(new ReservaRequest(productoId, pedidoId, 2)))
                .isInstanceOf(ReferenciaInvalidaException.class);
    }

    @Test 
    void liberarReservaActivaDevuelveElStock() {
        UUID reservaId = UUID.randomUUID();
        Reserva reserva = new Reserva(reservaId, productoId, pedidoId, 2, EstadoReserva.ACTIVA, LocalDateTime.now());
        Producto producto = new Producto(productoId, "SKU321", "Teclado", 15);
        when(reservaRepository.findByIdParaActualizar(reservaId)).thenReturn(Optional.of(reserva));
        when(productoRepository.findByIdParaActualizar(productoId)).thenReturn(Optional.of(producto));

        ReservaResponse response = reservaService.liberar(reservaId);

        assertThat(response.estado()).isEqualTo(EstadoReserva.LIBERADA);
        assertThat(producto.getCantidadDisponible()).isEqualTo(17);
    }

    @Test 
    void liberarReservaYaLiberadaNoVuelveASumarStock() {
        UUID reservaId = UUID.randomUUID();
        Reserva reserva = new Reserva(reservaId, productoId, pedidoId, 2, EstadoReserva.LIBERADA, LocalDateTime.now());
        when(reservaRepository.findByIdParaActualizar(reservaId)).thenReturn(Optional.of(reserva));

        ReservaResponse response = reservaService.liberar(reservaId);

        assertThat(response.estado()).isEqualTo(EstadoReserva.LIBERADA);
        verify(productoRepository, never()).findByIdParaActualizar(any());
    }

    @Test 
    void liberarReservaInexistenteLanzaNoEncontrado() {
        UUID reservaId = UUID.randomUUID();
        when(reservaRepository.findByIdParaActualizar(reservaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.liberar(reservaId))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void liberarPorPedidoLiberaSoloLasActivas() {
        UUID otroProductoId = UUID.randomUUID();
        Reserva activa = new Reserva(UUID.randomUUID(), productoId, pedidoId, 2, EstadoReserva.ACTIVA, LocalDateTime.now());
        Reserva liberada = new Reserva(UUID.randomUUID(), otroProductoId, pedidoId, 3, EstadoReserva.LIBERADA, LocalDateTime.now());
        Producto producto = new Producto(productoId, "SKU123", "Producto Test", 10);
        when(reservaRepository.findByPedidoIdParaActualizar(pedidoId)).thenReturn(List.of(activa, liberada));
        when(productoRepository.findByIdParaActualizar(productoId)).thenReturn(Optional.of(producto));

        ReservasResponse response = reservaService.liberarPorPedido(pedidoId);

        assertThat(response.reservas()).hasSize(2);
        assertThat(response.reservas()).allMatch(r -> r.estado() == EstadoReserva.LIBERADA);
        assertThat(producto.getCantidadDisponible()).isEqualTo(12);
        verify(productoRepository, never()).findByIdParaActualizar(otroProductoId);
    }

    @Test
    void liberarPorPedidoSinReservasDevuelveListaVacia() {
        when(reservaRepository.findByPedidoIdParaActualizar(pedidoId)).thenReturn(List.of());

        assertThat(reservaService.liberarPorPedido(pedidoId).reservas()).isEmpty();
    }
}
