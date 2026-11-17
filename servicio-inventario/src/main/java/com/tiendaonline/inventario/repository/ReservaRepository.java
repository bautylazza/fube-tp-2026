package com.tiendaonline.inventario.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.inventario.entity.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByPedidoId(UUID pedidoId);
}
