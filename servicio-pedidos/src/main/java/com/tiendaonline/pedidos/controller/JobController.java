package com.tiendaonline.pedidos.controller;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.pedidos.batch.ExpiracionPedidosLauncher;
import com.tiendaonline.pedidos.dto.EjecucionJobResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/jobs/expiracion-pedidos/ejecuciones")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Ejecución manual de procesos por lotes")
public class JobController {

    private final ExpiracionPedidosLauncher launcher;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Dispara a mano la expiración de pedidos")
    public EjecucionJobResponse ejecutar() throws JobExecutionException {
        JobExecution ejecucion = launcher.lanzar();
        return new EjecucionJobResponse(ejecucion.getId(), ejecucion.getStatus().name());
    }
}
