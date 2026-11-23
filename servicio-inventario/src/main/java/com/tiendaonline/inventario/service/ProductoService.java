package com.tiendaonline.inventario.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.inventario.dto.DisponibilidadResponse;
import com.tiendaonline.inventario.dto.ProductoResponse;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.exception.RecursoNoEncontradoException;
import com.tiendaonline.inventario.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProductoService {


    private final ProductoRepository productoRepository;

    @Transactional (readOnly = true)
    public ProductoResponse obtener(UUID id) {
        return ProductoResponse.fromEntity(buscar(id));
    }

    private Producto buscar(UUID id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
    }

    public DisponibilidadResponse obtenerDisponibilidad(UUID id) {
        Producto producto = buscar(id);
        int cantidadDisponible = producto.getCantidadDisponible();
        return new DisponibilidadResponse(producto.getId(), cantidadDisponible, cantidadDisponible > 0);
    }
}
