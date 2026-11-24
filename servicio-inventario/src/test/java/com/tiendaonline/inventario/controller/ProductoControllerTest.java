package com.tiendaonline.inventario.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tiendaonline.inventario.dto.DisponibilidadResponse;
import com.tiendaonline.inventario.dto.ProductoResponse;
import com.tiendaonline.inventario.exception.RecursoNoEncontradoException;
import com.tiendaonline.inventario.service.ProductoService;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockBean 
    private ProductoService productoService;

    private final UUID id = UUID.randomUUID();

    @Test 
    void obtenerDevuelve200ConElProducto() throws Exception {
        when(productoService.obtener(id))
                .thenReturn(new ProductoResponse(id, "SKU1", "Teclado Mecanico", 10));
        
        mockMvc.perform(get("/api/v1/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.sku").value("SKU1"))
                .andExpect(jsonPath("$.nombre").value("Teclado Mecanico"));
    }

    @Test 
    void disponibilidadDevuelve200() throws Exception {
        when(productoService.obtenerDisponibilidad(id))
                .thenReturn(new DisponibilidadResponse(id, 10, true));
        
        mockMvc.perform(get("/api/v1/productos/{id}/disponibilidad", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(id.toString()))
                .andExpect(jsonPath("$.cantidadDisponible").value(10))
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test 
    void productoInexistenteDevuelve404() throws Exception {
        when(productoService.obtener(id))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
        
        mockMvc.perform(get("/api/v1/productos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("Producto no encontrado con id: " + id));
    }

    @Test 
    void idQueNoEsUuidDevuelve400() throws Exception {
        String idInvalido = "12345";
        
        mockMvc.perform(get("/api/v1/productos/{id}", idInvalido))
                .andExpect(status().isBadRequest());
    }
}
