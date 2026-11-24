package com.tiendaonline.pedidos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.pedidos.client.ClientesClient;
import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.dto.CrearPedidoRequest;
import com.tiendaonline.pedidos.dto.PedidoResponse;
import com.tiendaonline.pedidos.dto.PedidosResponse;
import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.entity.Pedido;
import com.tiendaonline.pedidos.exception.ConflictoException;
import com.tiendaonline.pedidos.exception.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClientesClient clientesClient;
    private final InventarioClient inventarioClient;

    @Transactional
    public PedidoResponse crear(CrearPedidoRequest request) {
        clientesClient.verificarExiste(request.clienteId());

        // Se guarda antes de reservar: la reserva necesita el id del pedido
        // y un error de base aparece antes de tocar el stock.
        Pedido pedido = pedidoRepository.saveAndFlush(nuevoPedido(request));

        try {
            request.items().forEach(item ->
                    inventarioClient.reservar(pedido.getId(), item.productoId(), item.cantidad()));
        } catch (RuntimeException ex) {
            compensarReservas(pedido.getId());
            throw ex;
        }

        return PedidoResponse.fromEntity(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(UUID id) {
        return pedidoRepository.findById(id)
                .map(PedidoResponse::fromEntity)
                .orElseThrow(() -> noEncontrado(id));
    }

    @Transactional(readOnly = true)
    public PedidosResponse listarPorCliente(UUID clienteId) {
        return new PedidosResponse(
            pedidoRepository.findByClienteId(clienteId).stream()
                .map(PedidoResponse::fromEntity)
                .toList()
        );
    }

    @Transactional
    public PedidoResponse actualizarEstado(UUID id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> noEncontrado(id));

        if (pedido.getEstado() == nuevoEstado) {
            return PedidoResponse.fromEntity(pedido);
        }

        if (!pedido.getEstado().puedePasarA(nuevoEstado)) {
            throw new ConflictoException("Un pedido " + pedido.getEstado() + " no puede pasar a " + nuevoEstado);
        }

        if (nuevoEstado == EstadoPedido.CANCELADO) {
            inventarioClient.liberarPorPedido(id);
        }

        pedido.cambiarEstado(nuevoEstado);
        return PedidoResponse.fromEntity(pedido);
    }

    private Pedido nuevoPedido(CrearPedidoRequest request) {
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = new Pedido(null, request.clienteId(), new ArrayList<>(), EstadoPedido.CREADO, BigDecimal.ZERO, ahora, ahora);
        request.items().forEach(item ->
                pedido.agregarItem(item.productoId(), item.cantidad(), item.precioUnitario()));
        pedido.calcularMontoTotal();
        return pedido;
    }

    private void compensarReservas(UUID pedidoId) {
        try {
            inventarioClient.liberarPorPedido(pedidoId);
        } catch (RuntimeException ex) {
            // Se conserva el error original; las reservas quedan para liberar a mano.
            log.error("No se pudieron liberar las reservas del pedido {}", pedidoId, ex);
        }
    }

    private RecursoNoEncontradoException noEncontrado(UUID id) {
        return new RecursoNoEncontradoException("Pedido no encontrado: " + id);
    }
}
