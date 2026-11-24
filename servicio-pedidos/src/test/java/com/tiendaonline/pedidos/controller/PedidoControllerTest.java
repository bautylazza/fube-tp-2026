package com.tiendaonline.pedidos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.tiendaonline.pedidos.dto.ItemPedidoResponse;
import com.tiendaonline.pedidos.dto.PedidoResponse;
import com.tiendaonline.pedidos.dto.PedidosResponse;
import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.exception.ConflictoException;
import com.tiendaonline.pedidos.exception.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.exception.ReferenciaInvalidaException;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;
import com.tiendaonline.pedidos.exception.StockInsuficienteException;
import com.tiendaonline.pedidos.service.PedidoService;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    private final UUID pedidoId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    @Test
    void crearDevuelve201ConLocation() throws Exception {
        when(pedidoService.crear(any())).thenReturn(pedidoEn(EstadoPedido.CREADO));

        crear(bodyValido())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/pedidos/" + pedidoId))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.montoTotal").value(20.00))
                .andExpect(jsonPath("$.items[0].productoId").value(productoId.toString()));
    }

    @Test
    void crearSinClienteNiItemsDevuelve400ConTodosLosErrores() throws Exception {
        crear("{\"items\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.clienteId").exists())
                .andExpect(jsonPath("$.errors.items").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearConItemInvalidoDevuelve400() throws Exception {
        crear(body(item(productoId, 0, "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['items[0].cantidad']").exists())
                .andExpect(jsonPath("$.errors['items[0].precioUnitario']").exists());
    }

    @Test
    void crearConProductosRepetidosDevuelve400() throws Exception {
        crear(body(item(productoId, 1, "10") + "," + item(productoId, 2, "10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productosSinRepetir").exists());
    }

    @Test
    void crearConMontoQueSuperaElMaximoDevuelve400() throws Exception {
        crear(body(item(productoId, 2, "9999999999.99")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.montoTotalValido").exists());
    }

    @Test
    void crearConItemNuloDevuelve400() throws Exception {
        crear(body("null")).andExpect(status().isBadRequest());
    }

    @Test
    void crearConJsonMalFormadoDevuelve400() throws Exception {
        crear("{").andExpect(status().isBadRequest());
    }

    @Test
    void crearConClienteInexistenteDevuelve422() throws Exception {
        when(pedidoService.crear(any())).thenThrow(new ReferenciaInvalidaException("Cliente no encontrado"));

        crear(bodyValido()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void crearSinStockDevuelve409() throws Exception {
        when(pedidoService.crear(any())).thenThrow(new StockInsuficienteException("Stock insuficiente"));

        crear(bodyValido()).andExpect(status().isConflict());
    }

    @Test
    void crearConServicioCaidoDevuelve503() throws Exception {
        when(pedidoService.crear(any())).thenThrow(new ServicioNoDisponibleException("inventario caido", null));

        crear(bodyValido()).andExpect(status().isServiceUnavailable());
    }

    @Test
    void obtenerDevuelve200() throws Exception {
        when(pedidoService.obtener(pedidoId)).thenReturn(pedidoEn(EstadoPedido.CREADO));

        mockMvc.perform(get("/api/v1/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId.toString()));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(pedidoService.obtener(pedidoId)).thenThrow(new RecursoNoEncontradoException("Pedido no encontrado"));

        mockMvc.perform(get("/api/v1/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorClienteDevuelveLosPedidosDentroDeUnObjeto() throws Exception {
        when(pedidoService.listarPorCliente(clienteId))
                .thenReturn(new PedidosResponse(List.of(pedidoEn(EstadoPedido.CREADO))));

        mockMvc.perform(get("/api/v1/pedidos").param("clienteId", clienteId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidos[0].id").value(pedidoId.toString()));
    }

    @Test
    void listarSinClienteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = { "CANCELADO", "CONFIRMADO" })
    void actualizarEstadoDevuelve200(String estado) throws Exception {
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(estado);
        when(pedidoService.actualizarEstado(pedidoId, nuevoEstado)).thenReturn(pedidoEn(nuevoEstado));

        actualizar("{\"estado\":\"" + estado + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(estado));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"estado\":\"EXPIRADO\"}", "{\"estado\":\"CREADO\"}", "{\"estado\":\"OTRO\"}", "{}" })
    void actualizarAUnEstadoNoPermitidoDevuelve400(String body) throws Exception {
        actualizar(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.estado").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void transicionInvalidaDevuelve409() throws Exception {
        when(pedidoService.actualizarEstado(pedidoId, EstadoPedido.CANCELADO))
                .thenThrow(new ConflictoException("Un pedido EXPIRADO no puede pasar a CANCELADO"));

        actualizar("{\"estado\":\"CANCELADO\"}").andExpect(status().isConflict());
    }

    private ResultActions crear(String body) throws Exception {
        return mockMvc.perform(post("/api/v1/pedidos").contentType("application/json").content(body));
    }

    private ResultActions actualizar(String body) throws Exception {
        return mockMvc.perform(patch("/api/v1/pedidos/{id}", pedidoId).contentType("application/json").content(body));
    }

    private String bodyValido() {
        return body(item(productoId, 2, "10.00"));
    }

    private String body(String items) {
        return "{\"clienteId\":\"" + clienteId + "\",\"items\":[" + items + "]}";
    }

    private String item(UUID producto, int cantidad, String precio) {
        return "{\"productoId\":\"" + producto + "\",\"cantidad\":" + cantidad + ",\"precioUnitario\":" + precio + "}";
    }

    private PedidoResponse pedidoEn(EstadoPedido estado) {
        LocalDateTime ahora = LocalDateTime.now();
        return new PedidoResponse(pedidoId, clienteId, estado, new BigDecimal("20.00"), ahora, ahora,
                List.of(new ItemPedidoResponse(UUID.randomUUID(), productoId, 2, new BigDecimal("10.00"))));
    }
}
