package com.tiendaonline.inventario.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.inventario.dto.DisponibilidadResponse;
import com.tiendaonline.inventario.dto.ProductoResponse;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.exception.RecursoNoEncontradoException;
import com.tiendaonline.inventario.repository.ProductoRepository;

@ExtendWith (MockitoExtension.class)
public class ProductoServiceTest {


    @Mock ProductoRepository productoRepository;
    @InjectMocks ProductoService productoService;

    private final UUID productId = UUID.randomUUID();

    @Test
    void obtenerDevuelveElProducto() {
        Producto producto = new Producto(productId, "SKU1", "Teclado Mecanico", 10);
        when(productoRepository.findById(productId)).thenReturn(Optional.of(producto));

        ProductoResponse response = productoService.obtener(productId);

        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.sku()).isEqualTo("SKU1");
        assertThat(response.nombre()).isEqualTo("Teclado Mecanico");
        assertThat(response.cantidadDisponible()).isEqualTo(10);
    }

    @Test 
    void disponibilidadConStockDevuelveDisponible() {
        when(productoRepository.findById(productId)).thenReturn(Optional.of(new Producto(productId, "SKU1", "Teclado Mecanico", 10)));

        DisponibilidadResponse response = productoService.obtenerDisponibilidad(productId);

        assertThat(response.cantidadDisponible()).isEqualTo(10);
        assertThat(response.disponible()).isTrue();
    }

    @Test 
    void disponibilidadSinStockDevuelveNoDisponible() {
        when(productoRepository.findById(productId)).thenReturn(Optional.of(new Producto(productId, "SKU1", "Teclado Mecanico", 0)));

        DisponibilidadResponse response = productoService.obtenerDisponibilidad(productId);

        assertThat(response.cantidadDisponible()).isEqualTo(0);
        assertThat(response.disponible()).isFalse();
    }

    @Test 
    void productoNoEncontradoLanzaExcepcion() {
        when(productoRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtener(productId))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado con id: " + productId);
    }


}
