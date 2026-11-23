package com.tiendaonline.inventario.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.inventario.dto.ReservaRequest;
import com.tiendaonline.inventario.dto.ReservaResponse;
import com.tiendaonline.inventario.service.ReservaService;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/v1/reservas")
@RequiredArgsConstructor 
@Tag (name = "Reserva", description = "Disponibilidad y reserva de productos")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ReservaResponse> reservar(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse reservaResponse = reservaService.reservar(request);
        return ResponseEntity.created(URI.create("/api/v1/reservas/" + reservaResponse.id())).body(reservaResponse);
    }

}
