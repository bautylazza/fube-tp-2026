package com.tiendaonline.pedidos.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.pedidos.dto.ActualizarPedidoRequest;
import com.tiendaonline.pedidos.dto.CrearPedidoRequest;
import com.tiendaonline.pedidos.dto.PedidoResponse;
import com.tiendaonline.pedidos.dto.PedidosResponse;
import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.service.PedidoService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Creación, consulta y cambio de estado de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
        PedidoResponse creado = pedidoService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + creado.id())).body(creado);
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable UUID id) {
        return pedidoService.obtener(id);
    }

    @GetMapping
    public PedidosResponse listarPorCliente(@RequestParam UUID clienteId) {
        return pedidoService.listarPorCliente(clienteId);
    }

    @PatchMapping("/{id}")
    public PedidoResponse actualizarEstado(@PathVariable UUID id, @Valid @RequestBody ActualizarPedidoRequest request) {
        return pedidoService.actualizarEstado(id, EstadoPedido.valueOf(request.estado()));
    }
}
