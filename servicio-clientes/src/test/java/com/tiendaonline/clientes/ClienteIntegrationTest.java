package com.tiendaonline.clientes;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaonline.clientes.featureflag.FeatureFlags;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureFlags featureFlags;

    @Test
    void conMaskPiiActivaLaConsultaDevuelveNombreYEmailEnmascarados() throws Exception {
        String id = registrar("Juan Perez", "juan.mask@gmail.com");
        when(featureFlags.estaActiva(eq("mask-pii"), anyString())).thenReturn(true);

        mockMvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("J*** P***"))
                .andExpect(jsonPath("$.email").value("****@****.com"));
    }

    @Test
    void conMaskPiiInactivaLaConsultaDevuelveLosDatosOriginales() throws Exception {
        String id = registrar("Juan Perez", "juan.sinmask@gmail.com");
        when(featureFlags.estaActiva(eq("mask-pii"), anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan.sinmask@gmail.com"));
    }

    private String registrar(String nombre, String email) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombre", nombre, "email", email))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(respuesta);
        return json.get("id").asText();
    }
}
