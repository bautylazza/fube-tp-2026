package com.tiendaonline.pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.pedidos.client.ClientesClient;
import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.dto.CrearPedidoRequest;
import com.tiendaonline.pedidos.dto.ItemPedidoRequest;
import com.tiendaonline.pedidos.dto.PedidoResponse;
import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.entity.Pedido;
import com.tiendaonline.pedidos.exception.ConflictoException;
import com.tiendaonline.pedidos.exception.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;
import com.tiendaonline.pedidos.exception.StockInsuficienteException;
import com.tiendaonline.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final LocalDateTime FECHA_VIEJA = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClientesClient clientesClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoService pedidoService;

    private final UUID pedidoId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID producto1 = UUID.randomUUID();
    private final UUID producto2 = UUID.randomUUID();

    private final CrearPedidoRequest request = new CrearPedidoRequest(clienteId, List.of(
            new ItemPedidoRequest(producto1, 2, new BigDecimal("100.50")),
            new ItemPedidoRequest(producto2, 1, new BigDecimal("50.00"))));

    @Test
    void crearReservaCadaItemYCalculaElMonto() {
        simularGuardado();

        PedidoResponse response = pedidoService.crear(request);

        assertThat(response.id()).isEqualTo(pedidoId);
        assertThat(response.clienteId()).isEqualTo(clienteId);
        assertThat(response.estado()).isEqualTo(EstadoPedido.CREADO);
        assertThat(response.montoTotal()).isEqualByComparingTo("251.00");
        assertThat(response.items()).hasSize(2);
        assertThat(response.fechaCreacion()).isNotNull();
        verify(inventarioClient).reservar(pedidoId, producto1, 2);
        verify(inventarioClient).reservar(pedidoId, producto2, 1);
        verify(inventarioClient, never()).liberarPorPedido(any());
    }

    @Test
    void crearConClienteInexistenteNoGuardaNiReserva() {
        doThrow(new ReferenciaInvalidaException("Cliente no encontrado"))
                .when(clientesClient).verificarExiste(clienteId);

        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(ReferenciaInvalidaException.class);

        verifyNoInteractions(pedidoRepository, inventarioClient);
    }

    @Test
    void crearSinStockEnElSegundoItemLiberaLoReservado() {
        simularGuardado();
        // lenient: el primer item llama al mismo metodo con otros argumentos
        lenient().doThrow(new StockInsuficienteException("Stock insuficiente"))
                .when(inventarioClient).reservar(pedidoId, producto2, 1);

        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(StockInsuficienteException.class);

        verify(inventarioClient).reservar(pedidoId, producto1, 2);
        verify(inventarioClient).liberarPorPedido(pedidoId);
    }

    @Test
    void crearConInventarioCaidoConservaElErrorOriginalAunqueFalleLaCompensacion() {
        simularGuardado();
        ServicioNoDisponibleException original = new ServicioNoDisponibleException("inventario caido", null);
        doThrow(original).when(inventarioClient).reservar(pedidoId, producto1, 2);
        doThrow(new ServicioNoDisponibleException("tampoco libera", null))
                .when(inventarioClient).liberarPorPedido(pedidoId);

        assertThatThrownBy(() -> pedidoService.crear(request)).isSameAs(original);

        verify(inventarioClient, never()).reservar(pedidoId, producto2, 1);
    }

    @Test
    void obtenerDevuelveElPedidoConItems() {
        Pedido pedido = pedidoEn(EstadoPedido.CREADO);
        pedido.agregarItem(producto1, 2, new BigDecimal("10.00"));
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        PedidoResponse response = pedidoService.obtener(pedidoId);

        assertThat(response.id()).isEqualTo(pedidoId);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productoId()).isEqualTo(producto1);
    }

    @Test
    void obtenerInexistenteLanzaNoEncontrado() {
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtener(pedidoId))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarPorClienteDevuelveSusPedidos() {
        when(pedidoRepository.findByClienteId(clienteId)).thenReturn(List.of(pedidoEn(EstadoPedido.CREADO)));

        assertThat(pedidoService.listarPorCliente(clienteId).pedidos())
                .extracting(PedidoResponse::id)
                .containsExactly(pedidoId);
    }

    @Test
    void listarPorClienteSinPedidosDevuelveListaVacia() {
        when(pedidoRepository.findByClienteId(clienteId)).thenReturn(List.of());

        assertThat(pedidoService.listarPorCliente(clienteId).pedidos()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = { "CREADO", "CONFIRMADO" })
    void cancelarLiberaLasReservas(EstadoPedido estadoInicial) {
        simularBusqueda(estadoInicial);

        PedidoResponse response = pedidoService.actualizarEstado(pedidoId, EstadoPedido.CANCELADO);

        assertThat(response.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(response.fechaActualizacion()).isAfter(FECHA_VIEJA);
        verify(inventarioClient).liberarPorPedido(pedidoId);
    }

    @Test
    void cancelarUnPedidoYaCanceladoNoLlamaAInventario() {
        simularBusqueda(EstadoPedido.CANCELADO);

        PedidoResponse response = pedidoService.actualizarEstado(pedidoId, EstadoPedido.CANCELADO);

        assertThat(response.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(response.fechaActualizacion()).isEqualTo(FECHA_VIEJA);
        verifyNoInteractions(inventarioClient);
    }

    @Test
    void cancelarUnPedidoExpiradoLanzaConflicto() {
        simularBusqueda(EstadoPedido.EXPIRADO);

        assertThatThrownBy(() -> pedidoService.actualizarEstado(pedidoId, EstadoPedido.CANCELADO))
                .isInstanceOf(ConflictoException.class);

        verifyNoInteractions(inventarioClient);
    }

    @Test
    void cancelarConInventarioCaidoNoCambiaElEstado() {
        Pedido pedido = simularBusqueda(EstadoPedido.CREADO);
        doThrow(new ServicioNoDisponibleException("inventario caido", null))
                .when(inventarioClient).liberarPorPedido(pedidoId);

        assertThatThrownBy(() -> pedidoService.actualizarEstado(pedidoId, EstadoPedido.CANCELADO))
                .isInstanceOf(ServicioNoDisponibleException.class);

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CREADO);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = { "CREADO", "CONFIRMADO" })
    void confirmarNoTocaElStock(EstadoPedido estadoInicial) {
        simularBusqueda(estadoInicial);

        PedidoResponse response = pedidoService.actualizarEstado(pedidoId, EstadoPedido.CONFIRMADO);

        assertThat(response.estado()).isEqualTo(EstadoPedido.CONFIRMADO);
        verifyNoInteractions(inventarioClient);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = { "CANCELADO", "EXPIRADO" })
    void confirmarUnPedidoCerradoLanzaConflicto(EstadoPedido estadoInicial) {
        simularBusqueda(estadoInicial);

        assertThatThrownBy(() -> pedidoService.actualizarEstado(pedidoId, EstadoPedido.CONFIRMADO))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void actualizarUnPedidoInexistenteLanzaNoEncontrado() {
        when(pedidoRepository.findByIdParaActualizar(pedidoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.actualizarEstado(pedidoId, EstadoPedido.CANCELADO))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private void simularGuardado() {
        when(pedidoRepository.saveAndFlush(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido pedido = invocacion.getArgument(0);
            pedido.setId(pedidoId);
            return pedido;
        });
    }

    private Pedido simularBusqueda(EstadoPedido estado) {
        Pedido pedido = pedidoEn(estado);
        when(pedidoRepository.findByIdParaActualizar(pedidoId)).thenReturn(Optional.of(pedido));
        return pedido;
    }

    private Pedido pedidoEn(EstadoPedido estado) {
        return new Pedido(pedidoId, clienteId, new ArrayList<>(), estado, BigDecimal.ZERO, FECHA_VIEJA, FECHA_VIEJA);
    }
}
