package com.tiendaonline.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EjecucionJobResponse(
    @Schema(example = "1") Long id,
    @Schema(example = "STARTING") String estado
) {}
