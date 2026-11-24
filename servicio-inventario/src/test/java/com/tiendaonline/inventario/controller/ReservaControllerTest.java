package com.tiendaonline.inventario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tiendaonline.inventario.dto.ReservaResponse;
import com.tiendaonline.inventario.dto.ReservasResponse;
import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.service.ReservaService;
import com.tiendaonline.inventario.exception.RecursoNoEncontradoException;
import com.tiendaonline.inventario.exception.ReferenciaInvalidaException;
import com.tiendaonline.inventario.exception.StockInsuficienteException;

@WebMvcTest (ReservaController.class)
public class ReservaControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockBean 
    private ReservaService reservaService;

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

    @Test 
    void liberarDevuelve200() throws Exception {
        UUID id = UUID.randomUUID();
        

        when(reservaService.liberar(id))
                .thenReturn(new ReservaResponse(
                        id, UUID.randomUUID(), UUID.randomUUID(), 2, EstadoReserva.LIBERADA, LocalDateTime.now()));
        
        mockMvc.perform(patch("/api/v1/reservas/{id}", id)
                .contentType("application/json")
                .content("{\"estado\":\"LIBERADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.estado").value("LIBERADA"));
    }

    @Test 
    void liberarReservaInexistenteDevuelve404() throws Exception {
        UUID id = UUID.randomUUID();

        when(reservaService.liberar(id))
                .thenThrow(new RecursoNoEncontradoException("Reserva no encontrada: " + id));

        mockMvc.perform(patch("/api/v1/reservas/{id}", id)
                .contentType("application/json")
                .content("{\"estado\":\"LIBERADA\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
    }

    @Test 
    void actualizarAOtroEstadoDevuelve400() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/reservas/{id}", id)
                .contentType("application/json")
                .content("{\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.estado").exists());
    }

    @Test
    void liberarPorPedidoDevuelve200() throws Exception {
        UUID id = UUID.randomUUID(), pedidoId = UUID.randomUUID();

        when(reservaService.liberarPorPedido(pedidoId))
                .thenReturn(new ReservasResponse(List.of(new ReservaResponse(
                        id, UUID.randomUUID(), pedidoId, 2, EstadoReserva.LIBERADA, LocalDateTime.now()))));

        mockMvc.perform(patch("/api/v1/reservas")
                .param("pedidoId", pedidoId.toString())
                .contentType("application/json")
                .content("{\"estado\":\"LIBERADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservas[0].id").value(id.toString()))
                .andExpect(jsonPath("$.reservas[0].estado").value("LIBERADA"));
    }

    @Test
    void liberarPorPedidoSinPedidoIdDevuelve400() throws Exception {
        mockMvc.perform(patch("/api/v1/reservas")
                .contentType("application/json")
                .content("{\"estado\":\"LIBERADA\"}"))
                .andExpect(status().isBadRequest());
    }
}
