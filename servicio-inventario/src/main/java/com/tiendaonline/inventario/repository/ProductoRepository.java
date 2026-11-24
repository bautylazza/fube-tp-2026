package com.tiendaonline.inventario.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tiendaonline.inventario.entity.Producto;

import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdParaActualizar(@Param("id") UUID id);

}
