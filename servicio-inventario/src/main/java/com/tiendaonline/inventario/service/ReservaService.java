package com.tiendaonline.inventario.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.inventario.dto.ReservaRequest;
import com.tiendaonline.inventario.dto.ReservaResponse;
import com.tiendaonline.inventario.dto.ReservasResponse;
import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.entity.Reserva;
import com.tiendaonline.inventario.exception.RecursoNoEncontradoException;
import com.tiendaonline.inventario.exception.ReferenciaInvalidaException;
import com.tiendaonline.inventario.exception.StockInsuficienteException;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service 
@RequiredArgsConstructor 
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ProductoRepository productoRepository;

    @Transactional 
    public ReservaResponse reservar(ReservaRequest request) {
        Producto producto = productoRepository.findByIdParaActualizar(request.productoId())
                .orElseThrow(() -> {
                    log.warn("Reserva rechazada, producto inexistente {}", kv("productoId", request.productoId()));
                    return new ReferenciaInvalidaException("Producto no encontrado: " + request.productoId());
                });

        if (producto.getCantidadDisponible() < request.cantidad()) {
            log.warn("Stock insuficiente {} {} {} {}", kv("productoId", request.productoId()),
                    kv("pedidoId", request.pedidoId()), kv("cantidad", request.cantidad()),
                    kv("disponible", producto.getCantidadDisponible()));
            throw new StockInsuficienteException("No hay suficiente cantidad disponible para el producto: " + request.productoId());
        }

        producto.setCantidadDisponible(producto.getCantidadDisponible() - request.cantidad());
        productoRepository.save(producto);

        Reserva nuevaReserva = new Reserva(null, request.productoId(), request.pedidoId(), request.cantidad(), EstadoReserva.ACTIVA, LocalDateTime.now());
        Reserva guardada = reservaRepository.save(nuevaReserva);
        log.info("Reserva creada {} {} {} {}", kv("reservaId", guardada.getId()), kv("productoId", guardada.getProductoId()),
                kv("pedidoId", guardada.getPedidoId()), kv("cantidad", guardada.getCantidad()));
        return ReservaResponse.fromEntity(guardada);
    }

    @Transactional 
    public ReservaResponse liberar(UUID id) {
        Reserva reserva = reservaRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + id));

                return liberarReserva(reserva);
    }

    @Transactional 
    public ReservasResponse liberarPorPedido(UUID pedidoId) {
        return new ReservasResponse(
            reservaRepository.findByPedidoIdParaActualizar(pedidoId).stream()
                .map(this::liberarReserva)
                .toList()
        );
    }

    private ReservaResponse liberarReserva(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.LIBERADA) {
            return ReservaResponse.fromEntity(reserva);
        }

        Producto producto = productoRepository.findByIdParaActualizar(reserva.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado para la reserva: " + reserva.getProductoId()));

        producto.setCantidadDisponible(producto.getCantidadDisponible() + reserva.getCantidad());
        productoRepository.save(producto);

        reserva.setEstado(EstadoReserva.LIBERADA);
        log.info("Reserva liberada {} {} {}", kv("reservaId", reserva.getId()), kv("productoId", reserva.getProductoId()),
                kv("pedidoId", reserva.getPedidoId()));
        return ReservaResponse.fromEntity(reserva);
    }
}
