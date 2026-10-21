package com.tiendaonline.inventario.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.inventario.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {

}
