package com.tiendaonline.clientes.featureflag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatsigFeatureFlagsTest {

    @Test
    void sinClaveNoInicializaStatsigYLosFlagsQuedanInactivos() {
        StatsigFeatureFlags featureFlags = new StatsigFeatureFlags("", "development");

        featureFlags.inicializar();

        assertThat(featureFlags.estaActiva("mask-pii", "cualquier-usuario")).isFalse();
        featureFlags.cerrar();
    }
}
