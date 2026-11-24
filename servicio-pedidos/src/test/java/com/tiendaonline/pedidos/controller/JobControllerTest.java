package com.tiendaonline.pedidos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tiendaonline.pedidos.batch.ExpiracionPedidosLauncher;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpiracionPedidosLauncher launcher;

    @Test
    void ejecutarDevuelve202ConLaEjecucion() throws Exception {
        when(launcher.lanzar()).thenReturn(new JobExecution(7L));

        mockMvc.perform(post("/api/v1/jobs/expiracion-pedidos/ejecuciones"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value("STARTING"));
    }
}
