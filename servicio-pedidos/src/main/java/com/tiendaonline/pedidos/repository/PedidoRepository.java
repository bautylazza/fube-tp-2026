package com.tiendaonline.pedidos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tiendaonline.pedidos.entity.Pedido;

import jakarta.persistence.LockModeType;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByClienteId(UUID clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> findByIdParaActualizar(UUID id);

    @Modifying
    @Query("""
            update Pedido p
            set p.estado = com.tiendaonline.pedidos.entity.EstadoPedido.EXPIRADO,
                p.fechaActualizacion = :fecha
            where p.id = :id
              and p.estado = com.tiendaonline.pedidos.entity.EstadoPedido.CREADO""")
    int expirarSiSigueCreado(UUID id, LocalDateTime fecha);
}
