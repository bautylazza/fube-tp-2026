package com.tiendaonline.clientes.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente registrado {}", kv("clienteId", guardado.getId()));
        return ClienteResponse.desde(guardado);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(UUID id) {
        ClienteResponse cliente = clienteRepository.findById(id)
                .map(ClienteResponse::desde)
                .orElseThrow(() -> clienteNoEncontrado(id));
        boolean enmascarado = featureFlags.estaActiva(FeatureFlags.MASK_PII, id.toString());
        log.info("Cliente consultado {} {}", kv("clienteId", id), kv("maskPii", enmascarado));
        return enmascarado ? enmascarar(cliente) : cliente;
    }

    public PedidosResponse listarPedidos(UUID id) {
        if (!clienteRepository.existsById(id)) {
            throw clienteNoEncontrado(id);
        }
        return pedidosClient.listarPorCliente(id);
    }

    private RecursoNoEncontradoException clienteNoEncontrado(UUID id) {
        log.warn("Cliente no encontrado {}", kv("clienteId", id));
        return new RecursoNoEncontradoException("No existe el cliente " + id);
    }

    private ClienteResponse enmascarar(ClienteResponse cliente) {
        return new ClienteResponse(cliente.id(), enmascaradorPii.nombre(cliente.nombre()),
                enmascaradorPii.email(cliente.email()), cliente.fechaAlta());
    }
}
