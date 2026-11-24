package com.tiendaonline.clientes.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class EnmascaradorPii {

    private static final String ASTERISCOS = "***";
    private static final String EMAIL_ENMASCARADO = "****@****";

    public String nombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        return Arrays.stream(nombre.trim().split("\\s+"))
                .filter(palabra -> !palabra.isEmpty())
                .map(palabra -> palabra.charAt(0) + ASTERISCOS)
                .collect(Collectors.joining(" "));
    }

    public String email(String email) {
        if (email == null) {
            return null;
        }
        String dominio = email.substring(email.indexOf('@') + 1);
        int ultimoPunto = dominio.lastIndexOf('.');
        if (ultimoPunto < 0) {
            return EMAIL_ENMASCARADO;
        }
        return EMAIL_ENMASCARADO + dominio.substring(ultimoPunto);
    }
}
