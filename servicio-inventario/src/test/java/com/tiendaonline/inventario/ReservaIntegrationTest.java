package com.tiendaonline.inventario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

@SpringBootTest 
@AutoConfigureMockMvc 
public class ReservaIntegrationTest {

    @Autowired 
    private MockMvc mockMvc;

    @Autowired 
    private ProductoRepository productoRepository;

    @Autowired 
    private ReservaRepository reservaRepository;

    private final UUID pedidoId = UUID.randomUUID();
    private UUID productoId;

    @BeforeEach
    void setUp() {
        productoId = productoRepository.save(new Producto(null, "INTEGRATION", "Monitor", 3)).getId();
    }

    @AfterEach
    void tearDown() {
        reservaRepository.deleteAll(reservaRepository.findByPedidoId(pedidoId));
        productoRepository.deleteById(productoId);
    }

    @Test
    void reservarYLiberarActualizaLaDisponibilidad() throws Exception {
        String body = mockMvc.perform(post("/api/v1/reservas")
                .contentType("application/json")
                .content("{\"productoId\":\"" + productoId + "\", \"pedidoId\":\"" + pedidoId + "\", \"cantidad\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String reservaId = JsonPath.read(body, "$.id");

        verificarDisponibilidad(2);

        mockMvc.perform(patch("/api/v1/reservas/{id}", reservaId)
                .contentType("application/json")
                .content("{\"estado\":\"LIBERADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LIBERADA"));

        verificarDisponibilidad(3);
    }

    private void verificarDisponibilidad(int cantidad) throws Exception {
        mockMvc.perform(get("/api/v1/productos/{id}/disponibilidad", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDisponible").value(cantidad));
    }

}
