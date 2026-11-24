package com.tiendaonline.inventario.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.tiendaonline.inventario.entity.EstadoReserva;
import com.tiendaonline.inventario.entity.Producto;
import com.tiendaonline.inventario.entity.Reserva;

import jakarta.persistence.LockModeType;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservaRepositoryTest {

    @Autowired 
    private ReservaRepository reservaRepository;

    @Autowired 
    private TestEntityManager em;

    @Test 
    void buscarParaActualizarBloqueaElProducto(){
        UUID id = UUID.randomUUID();
        Producto producto = em.persistAndFlush(new Producto(null, "TESTLOCK", "Teclado" , 10));
        Reserva guardada = em.persistAndFlush(new Reserva(null, producto.getId(), id, 10, EstadoReserva.ACTIVA,LocalDateTime.now()));
        em.clear();

        Reserva encontrado = reservaRepository.findByIdParaActualizar(guardada.getId()).orElseThrow();

        assertThat(encontrado.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
        assertThat(em.getEntityManager().getLockMode(encontrado)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void buscarPorPedidoParaActualizarBloqueaSoloLasDelPedido() {
        UUID pedidoId = UUID.randomUUID();
        Producto producto = em.persistAndFlush(new Producto(null, "TESTLOCK2", "Teclado", 10));
        Reserva delPedido = em.persistAndFlush(new Reserva(null, producto.getId(), pedidoId, 2, EstadoReserva.ACTIVA, LocalDateTime.now()));
        em.persistAndFlush(new Reserva(null, producto.getId(), UUID.randomUUID(), 3, EstadoReserva.ACTIVA, LocalDateTime.now()));
       em.clear();

        List<Reserva> encontradas = reservaRepository.findByPedidoIdParaActualizar(pedidoId);

        assertThat(encontradas).extracting(Reserva::getId).containsExactly(delPedido.getId());
        assertThat(em.getEntityManager().getLockMode(encontradas.get(0))).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
