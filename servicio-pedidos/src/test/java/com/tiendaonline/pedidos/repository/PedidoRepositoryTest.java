package com.tiendaonline.pedidos.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.entity.Pedido;

import jakarta.persistence.LockModeType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private TestEntityManager em;

    private final UUID clienteId = UUID.randomUUID();

    @Test
    void buscarPorClienteDevuelveSoloSusPedidosConItems() {
        Pedido delCliente = guardar(clienteId);
        guardar(UUID.randomUUID());
        em.clear();

        List<Pedido> encontrados = pedidoRepository.findByClienteId(clienteId);

        assertThat(encontrados).extracting(Pedido::getId).containsExactly(delCliente.getId());
        assertThat(em.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil()
                .isLoaded(encontrados.get(0), "items")).isTrue();
        assertThat(encontrados.get(0).getItems()).hasSize(2);
    }

    @Test
    void buscarParaActualizarBloqueaElPedido() {
        Pedido guardado = guardar(clienteId);
        em.clear();

        Pedido encontrado = pedidoRepository.findByIdParaActualizar(guardado.getId()).orElseThrow();

        assertThat(em.getEntityManager().getLockMode(encontrado)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void expirarCambiaElEstadoSoloSiSigueCreado() {
        Pedido creado = guardar(clienteId);
        Pedido confirmado = guardar(clienteId);
        confirmado.cambiarEstado(EstadoPedido.CONFIRMADO);
        em.flush();

        assertThat(pedidoRepository.expirarSiSigueCreado(creado.getId(), LocalDateTime.now())).isEqualTo(1);
        assertThat(pedidoRepository.expirarSiSigueCreado(confirmado.getId(), LocalDateTime.now())).isZero();

        em.clear();
        assertThat(em.find(Pedido.class, creado.getId()).getEstado()).isEqualTo(EstadoPedido.EXPIRADO);
        assertThat(em.find(Pedido.class, confirmado.getId()).getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
    }

    private Pedido guardar(UUID cliente) {
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = new Pedido(null, cliente, new ArrayList<>(), EstadoPedido.CREADO, BigDecimal.ZERO, ahora, ahora);
        pedido.agregarItem(UUID.randomUUID(), 1, new BigDecimal("10.00"));
        pedido.agregarItem(UUID.randomUUID(), 2, new BigDecimal("20.00"));
        return em.persistAndFlush(pedido);
    }
}
