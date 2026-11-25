package com.tiendaonline.clientes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.clientes.client.PedidosClient;
import com.tiendaonline.clientes.dto.ClienteResponse;
import com.tiendaonline.clientes.dto.CrearClienteRequest;
import com.tiendaonline.clientes.dto.PedidosResponse;
import com.tiendaonline.clientes.entity.Cliente;
import com.tiendaonline.clientes.exception.ConflictoException;
import com.tiendaonline.clientes.exception.RecursoNoEncontradoException;
import com.tiendaonline.clientes.exception.ServicioNoDisponibleException;
import com.tiendaonline.clientes.featureflag.FeatureFlags;
import com.tiendaonline.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private FeatureFlags featureFlags;

    @Spy
    private EnmascaradorPii enmascaradorPii;

    @Mock
    private PedidosClient pedidosClient;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void registraClienteValidoConFechaAlta() {
        CrearClienteRequest request = new CrearClienteRequest("Juan Perez", "juan@gmail.com");
        when(clienteRepository.existsByEmail("juan@gmail.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ClienteResponse response = clienteService.registrar(request);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getFechaAlta()).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.nombre()).isEqualTo("Juan Perez");
        assertThat(response.email()).isEqualTo("juan@gmail.com");
    }

    @Test
    void lanzaConflictoSiElEmailYaExiste() {
        CrearClienteRequest request = new CrearClienteRequest("Juan Perez", "juan@gmail.com");
        when(clienteRepository.existsByEmail("juan@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.registrar(request))
                .isInstanceOf(ConflictoException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void obtenerDevuelveElCliente() {
        UUID id = UUID.randomUUID();
        LocalDateTime alta = LocalDateTime.now();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(new Cliente(id, "Ana", "ana@mail.com", alta)));

        ClienteResponse response = clienteService.obtener(id);

        assertThat(response).isEqualTo(new ClienteResponse(id, "Ana", "ana@mail.com", alta));
    }

    @Test
    void obtenerConMaskPiiActivaEnmascaraNombreYEmail() {
        UUID id = UUID.randomUUID();
        LocalDateTime alta = LocalDateTime.now();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(new Cliente(id, "Juan Perez", "juan@gmail.com", alta)));
        when(featureFlags.estaActiva("mask-pii", id.toString())).thenReturn(true);

        ClienteResponse response = clienteService.obtener(id);

        assertThat(response).isEqualTo(new ClienteResponse(id, "J*** P***", "****@****.com", alta));
    }

    @Test
    void obtenerConMaskPiiInactivaDevuelveLosDatosSinEnmascarar() {
        UUID id = UUID.randomUUID();
        LocalDateTime alta = LocalDateTime.now();
        when(clienteRepository.findById(id)).thenReturn(Optional.of(new Cliente(id, "Juan Perez", "juan@gmail.com", alta)));
        when(featureFlags.estaActiva("mask-pii", id.toString())).thenReturn(false);

        ClienteResponse response = clienteService.obtener(id);

        assertThat(response).isEqualTo(new ClienteResponse(id, "Juan Perez", "juan@gmail.com", alta));
    }

    @Test
    void obtenerInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtener(id))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarPedidosDevuelveLoQueRespondeServicioPedidos() {
        UUID id = UUID.randomUUID();
        PedidosResponse pedidos = new PedidosResponse(List.of());
        when(clienteRepository.existsById(id)).thenReturn(true);
        when(pedidosClient.listarPorCliente(id)).thenReturn(pedidos);

        assertThat(clienteService.listarPedidos(id)).isSameAs(pedidos);
    }

    @Test
    void listarPedidosDeClienteInexistenteLanzaNoEncontradoSinLlamarAPedidos() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.listarPedidos(id))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(pedidosClient, never()).listarPorCliente(any());
    }

    @Test
    void listarPedidosPropagaElErrorSiServicioPedidosNoResponde() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.existsById(id)).thenReturn(true);
        when(pedidosClient.listarPorCliente(id))
                .thenThrow(new ServicioNoDisponibleException("servicio-pedidos no responde", null));

        assertThatThrownBy(() -> clienteService.listarPedidos(id))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
