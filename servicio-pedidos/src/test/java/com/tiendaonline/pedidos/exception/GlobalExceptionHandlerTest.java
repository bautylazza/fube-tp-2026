package com.tiendaonline.pedidos.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach 
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test 
    void conflictoDevuelve409ConProblemDetails() throws Exception {
        mockMvc.perform(get("/prueba/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflicto"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Conflicto de prueba."));
    }

    @Test
    void recursoNoEncontradoDevuelve404ConProblemDetails() throws Exception {
        mockMvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Recurso de prueba no encontrado."))
                .andExpect(jsonPath("$.instance").value("/prueba/no-encontrado"));
    }

    @Test 
    void idQueNoEsUuidDevuelve400() throws Exception {
        mockMvc.perform(get("/prueba/no-es-un-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test 
    void bodyInvalidoDevuelve400ConErroresPorCampo() throws Exception {

        String bodyInvalido = """
                { "nombre": "", "cantidad": -1 }
                """;

        mockMvc.perform(post("/prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.cantidad").exists());
    }

    @Test
    void referenciaInvalidaDevuelve422ConProblemDetails() throws Exception {
        mockMvc.perform(get("/prueba/referencia"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Referencia invalida"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("Cliente de prueba inexistente."));
    }

    @Test
    void servicioNoDisponibleDevuelve503ConProblemDetails() throws Exception {
        mockMvc.perform(get("/prueba/caido"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Servicio no disponible"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value("Servicio de prueba caido."));
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorDePrueba {

        @GetMapping("/conflicto")
        String conflicto() {
            throw new ConflictoException("Conflicto de prueba.");
        }

        @GetMapping("/no-encontrado")
        String noEncontrado() {
            throw new RecursoNoEncontradoException("Recurso de prueba no encontrado.");
        }

        @GetMapping("/{id}")
        String noEsUnUuid(@PathVariable UUID id) {
            return "ok";
        }

        @PostMapping
        String bodyInvalido(@Valid @RequestBody ObjetoDePrueba objeto) {
            return "ok";
        }

        @GetMapping("/referencia")
        String referencia() {
            throw new ReferenciaInvalidaException("Cliente de prueba inexistente.");
        }

        @GetMapping("/caido")
        String caido() {
            throw new ServicioNoDisponibleException("Servicio de prueba caido.", null);
        }

    }
}
