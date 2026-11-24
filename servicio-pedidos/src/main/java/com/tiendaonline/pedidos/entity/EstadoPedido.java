package com.tiendaonline.pedidos.entity;

public enum EstadoPedido {
    CREADO,
    CONFIRMADO,
    CANCELADO,
    EXPIRADO;

    public boolean puedePasarA(EstadoPedido destino) {
        return switch (this) {
            case CREADO -> destino == CONFIRMADO || destino == CANCELADO || destino == EXPIRADO;
            case CONFIRMADO -> destino == CANCELADO;
            case CANCELADO, EXPIRADO -> false;
        };
    }
}
