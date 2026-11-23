package com.tiendaonline.inventario.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.inventario.dto.ReservaRequest;
import com.tiendaonline.inventario.dto.ReservaResponse;
import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.entity.Reserva;
import com.tiendaonline.inventario.exception.ReferenciaInvalidaException;
import com.tiendaonline.inventario.exception.StockInsuficienteException;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ProductoRepository productoRepository;

    @Transactional 
    public ReservaResponse reservar(ReservaRequest request) {
        Producto producto = productoRepository.findByIdParaActualizar(request.productoId())
                .orElseThrow(() -> new ReferenciaInvalidaException("Producto no encontrado: " + request.productoId()));

        if (producto.getCantidadDisponible() < request.cantidad()) {
            throw new StockInsuficienteException("No hay suficiente cantidad disponible para el producto: " + request.productoId());
        }

        producto.setCantidadDisponible(producto.getCantidadDisponible() - request.cantidad());
        productoRepository.save(producto);

        Reserva nuevaReserva = new Reserva(null, request.productoId(), request.pedidoId(), request.cantidad(), EstadoReserva.ACTIVA, LocalDateTime.now());
        return ReservaResponse.fromEntity(reservaRepository.save(nuevaReserva));
    }
}
