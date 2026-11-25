package com.tiendaonline.inventario.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.inventario.dto.DisponibilidadResponse;
import com.tiendaonline.inventario.dto.ProductoResponse;
import com.tiendaonline.inventario.service.ProductoService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/v1/productos")
@RequiredArgsConstructor 
@Tag(name = "Producto", description = "Operaciones relacionadas con productos")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping ("/{id}")
    public ProductoResponse obtener(@PathVariable("id") UUID id) {
        return productoService.obtener(id);
    }

    @GetMapping ("/{id}/disponibilidad")
    public DisponibilidadResponse disponibilidad(@PathVariable("id") UUID id) {
        return productoService.obtenerDisponibilidad(id);
    }
}
