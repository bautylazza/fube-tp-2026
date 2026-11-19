package com.tiendaonline.clientes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.clientes.dto.ClienteResponse;
import com.tiendaonline.clientes.dto.CrearClienteRequest;
import com.tiendaonline.clientes.entity.Cliente;
import com.tiendaonline.clientes.exception.ConflictoException;
import com.tiendaonline.clientes.exception.RecursoNoEncontradoException;
import com.tiendaonline.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

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
    void obtenerInexistenteLanzaNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtener(id))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
