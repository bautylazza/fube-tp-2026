package com.tiendaonline.clientes.featureflag;

public interface FeatureFlags {

    String MASK_PII = "mask-pii";

    boolean estaActiva(String nombreFlag, String userId);
}
