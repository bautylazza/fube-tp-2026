package com.tiendaonline.inventario.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.inventario.repository.ProductoRepository;

@ExtendWith (MockitoExtension.class)
public class ProductServiceTest {


    @Mock ProductoRepository productoRepository;
    @InjectMocks ProductService productService;

    private final UUID productId = UUID.randomUUID();

    @Test
    void obtenerDevuelveElProducto() {
        // Implementar prueba para obtener un producto
    }

    @Test 
    void disponibilidadConStockDevuelveDisponible() {
        // Implementar prueba para verificar disponibilidad con stock
    }

    @Test 
    void disponibilidadSinStockDevuelveNoDisponible() {
        // Implementar prueba para verificar disponibilidad sin stock
    }

    @Test 
    void productoNoEncontradoLanzaExcepcion() {
        // Implementar prueba para verificar que se lanza una excepción cuando el producto no se encuentra
    }

    
}
