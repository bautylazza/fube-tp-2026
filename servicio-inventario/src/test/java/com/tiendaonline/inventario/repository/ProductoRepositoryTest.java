package com.tiendaonline.inventario.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.tiendaonline.inventario.entity.Producto;

import jakarta.persistence.LockModeType;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductoRepositoryTest {

    @Autowired 
    private ProductoRepository productoRepository;

    @Autowired 
    private TestEntityManager em;

    @Test 
    void buscarParaActualizarBloqueaElProducto(){
        Producto guardado = em.persistAndFlush(new Producto(null, "TESTLOCK", "Teclado" , 10));
        em.clear();

        Producto encontrado = productoRepository.findByIdParaActualizar(guardado.getId()).orElseThrow();

        assertThat(encontrado.getCantidadDisponible()).isEqualTo(10);
        assertThat(em.getEntityManager().getLockMode(encontrado)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
