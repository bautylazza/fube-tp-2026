package com.tiendaonline.inventario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tiendaonline.inventario.dto.ReservaResponse;
import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.exception.GlobalExceptionHandler;
import com.tiendaonline.inventario.service.ReservaService;
import com.tiendaonline.inventario.exception.ReferenciaInvalidaException;
import com.tiendaonline.inventario.exception.StockInsuficienteException;

@ExtendWith(MockitoExtension.class)
public class ReservaControllerTest {

    @Mock ReservaService reservaService;
    @InjectMocks ReservaController reservaController;
    MockMvc mockMvc;

    @BeforeEach 
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void reservarDevuelve201() throws Exception {
        UUID productoId = UUID.randomUUID(), pedidoId = UUID.randomUUID(), id = UUID.randomUUID();

        when(reservaService.reservar(any()))
                .thenReturn(new ReservaResponse(
                        id, productoId, pedidoId, 2, EstadoReserva.ACTIVA, LocalDateTime.now()));
        
        mockMvc.perform(post("/api/v1/reservas")
                .contentType("application/json")
                .content("{\"productoId\":\"" + productoId + "\", \"pedidoId\":\"" + pedidoId + "\", \"cantidad\":2}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/reservas/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.productoId").value(productoId.toString()))
                .andExpect(jsonPath("$.pedidoId").value(pedidoId.toString()))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void reservarSinStockDevuelve409() throws Exception {
        UUID productoId = UUID.randomUUID(), pedidoId = UUID.randomUUID();

        when(reservaService.reservar(any()))
                .thenThrow(new StockInsuficienteException("No hay suficiente cantidad disponible para el producto: " + productoId));
        
        mockMvc.perform(post("/api/v1/reservas")
                .contentType("application/json")
                .content("{\"productoId\":\"" + productoId + "\", \"pedidoId\":\"" + pedidoId + "\", \"cantidad\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No hay suficiente cantidad disponible para el producto: " + productoId));
    }

    @Test
    void reservarConCantidadCeroDevuelve400() throws Exception {
        UUID productoId = UUID.randomUUID(), pedidoId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/reservas")
                .contentType("application/json")
                .content("{\"productoId\":\"" + productoId + "\", \"pedidoId\":\"" + pedidoId + "\", \"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cantidad").exists());
    }

    @Test
    void reservarProductoInexistenteDevuelve422() throws Exception {
        UUID productoId = UUID.randomUUID(), pedidoId = UUID.randomUUID();

        when(reservaService.reservar(any()))
                .thenThrow(new ReferenciaInvalidaException("Producto no encontrado: " + productoId));

        mockMvc.perform(post("/api/v1/reservas")
                .contentType("application/json")
                .content("{\"productoId\":\"" + productoId + "\", \"pedidoId\":\"" + pedidoId + "\", \"cantidad\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Referencia invalida"));
    }

}
