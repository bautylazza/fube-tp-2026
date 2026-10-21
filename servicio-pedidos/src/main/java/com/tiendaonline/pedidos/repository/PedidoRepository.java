package com.tiendaonline.pedidos.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.pedidos.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByClienteID(UUID ClienteId);

}
