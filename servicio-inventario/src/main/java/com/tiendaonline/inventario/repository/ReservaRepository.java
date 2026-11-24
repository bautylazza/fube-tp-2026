package com.tiendaonline.inventario.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tiendaonline.inventario.entity.Reserva;

import jakarta.persistence.LockModeType;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByPedidoId(UUID pedidoId);

    @Lock (LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT r FROM Reserva r WHERE r.id = :id")
    Optional<Reserva> findByIdParaActualizar(@Param("id") UUID id);

    @Lock (LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT r FROM Reserva r WHERE r.pedidoId = :pedidoId ORDER BY r.productoId")
    List<Reserva> findByPedidoIdParaActualizar(@Param("pedidoId") UUID pedidoId);
}
