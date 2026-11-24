package com.tiendaonline.clientes.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.clientes.dto.ClienteResponse;
import com.tiendaonline.clientes.dto.CrearClienteRequest;
import com.tiendaonline.clientes.dto.PedidosResponse;
import com.tiendaonline.clientes.service.ClienteService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody CrearClienteRequest request) {
        ClienteResponse creado = clienteService.registrar(request);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + creado.id())).body(creado);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable UUID id) {
        return clienteService.obtener(id);
    }

    @GetMapping("/{id}/pedidos")
    public PedidosResponse pedidos(@PathVariable UUID id) {
        return clienteService.listarPedidos(id);
    }
}
