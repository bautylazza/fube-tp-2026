package com.tiendaonline.clientes.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.clientes.client.PedidosClient;
import com.tiendaonline.clientes.dto.ClienteResponse;
import com.tiendaonline.clientes.dto.CrearClienteRequest;
import com.tiendaonline.clientes.dto.PedidosResponse;
import com.tiendaonline.clientes.entity.Cliente;
import com.tiendaonline.clientes.exception.ConflictoException;
import com.tiendaonline.clientes.exception.RecursoNoEncontradoException;
import com.tiendaonline.clientes.featureflag.FeatureFlags;
import com.tiendaonline.clientes.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final FeatureFlags featureFlags;
    private final EnmascaradorPii enmascaradorPii;
    private final PedidosClient pedidosClient;

    @Transactional
    public ClienteResponse registrar(CrearClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new ConflictoException("Ya existe un cliente con el email " + request.email());
        }
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setFechaAlta(LocalDateTime.now());
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(UUID id) {
        ClienteResponse cliente = clienteRepository.findById(id)
                .map(ClienteResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente " + id));
        if (featureFlags.estaActiva(FeatureFlags.MASK_PII, id.toString())) {
            return enmascarar(cliente);
        }
        return cliente;
    }

    public PedidosResponse listarPedidos(UUID id) {
        if (!clienteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el cliente " + id);
        }
        return pedidosClient.listarPorCliente(id);
    }

    private ClienteResponse enmascarar(ClienteResponse cliente) {
        return new ClienteResponse(cliente.id(), enmascaradorPii.nombre(cliente.nombre()),
                enmascaradorPii.email(cliente.email()), cliente.fechaAlta());
    }
}
