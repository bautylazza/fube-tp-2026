package com.tiendaonline.clientes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.tiendaonline.clientes.dto.ClienteResponse;
import com.tiendaonline.clientes.dto.PedidoResponse;
import com.tiendaonline.clientes.dto.PedidosResponse;
import com.tiendaonline.clientes.exception.ConflictoException;
import com.tiendaonline.clientes.exception.RecursoNoEncontradoException;
import com.tiendaonline.clientes.exception.ServicioNoDisponibleException;
import com.tiendaonline.clientes.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @Test
    void crearDevuelve201ConLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.registrar(any()))
                .thenReturn(new ClienteResponse(id, "Juan Perez", "juan@gmail.com", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan Perez\",\"email\":\"juan@gmail.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/clientes/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void crearConDatosInvalidosDevuelve400ConTodosLosErrores() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"email\":\"no-es-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void crearConEmailRepetidoDevuelve409() throws Exception {
        when(clienteService.registrar(any())).thenThrow(new ConflictoException("email repetido"));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan Perez\",\"email\":\"juan@gmail.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.obtener(id)).thenThrow(new RecursoNoEncontradoException("No existe el cliente " + id));

        mockMvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe el cliente " + id));
    }

    @Test
    void obtenerConIdInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pedidosDelClienteDevuelve200ConLaLista() throws Exception {
        UUID id = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        LocalDateTime ahora = LocalDateTime.now();
        when(clienteService.listarPedidos(id)).thenReturn(new PedidosResponse(List.of(
                new PedidoResponse(pedidoId, id, "CREADO", new BigDecimal("100.00"), ahora, ahora, List.of()))));

        mockMvc.perform(get("/api/v1/clientes/{id}/pedidos", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidos[0].id").value(pedidoId.toString()));
    }

    @Test
    void pedidosDeClienteInexistenteDevuelve404() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.listarPedidos(id)).thenThrow(new RecursoNoEncontradoException("No existe el cliente " + id));

        mockMvc.perform(get("/api/v1/clientes/{id}/pedidos", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void pedidosConServicioPedidosCaidoDevuelve503() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.listarPedidos(id))
                .thenThrow(new ServicioNoDisponibleException("servicio-pedidos no responde", null));

        mockMvc.perform(get("/api/v1/clientes/{id}/pedidos", id))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
