package com.tiendaonline.clientes.featureflag;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.statsig.sdk.Statsig;
import com.statsig.sdk.StatsigOptions;
import com.statsig.sdk.StatsigUser;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StatsigFeatureFlags implements FeatureFlags {

    private static final long TIMEOUT_INICIALIZACION_SEGUNDOS = 10;

    private final String serverSecret;
    private final String environment;
    private boolean inicializado;

    public StatsigFeatureFlags(@Value("${statsig.server-secret:}") String serverSecret,
            @Value("${statsig.environment:development}") String environment) {
        this.serverSecret = serverSecret;
        this.environment = environment;
    }

    @PostConstruct
    public void inicializar() {
        if (serverSecret == null || serverSecret.isBlank()) {
            log.warn("STATSIG_SERVER_SECRET no configurada: los feature flags quedan inactivos");
            return;
        }
        try {
            StatsigOptions options = new StatsigOptions();
            options.setTier(environment);
            Statsig.initializeAsync(serverSecret, options).get(TIMEOUT_INICIALIZACION_SEGUNDOS, TimeUnit.SECONDS);
            inicializado = true;
            log.info("Statsig inicializado en el entorno {}", environment);
        } catch (Exception ex) {
            log.warn("No se pudo inicializar Statsig: los feature flags quedan inactivos", ex);
        }
    }

    // Si Statsig no esta disponible el flag se evalua como inactivo (datos sin enmascarar).
    @Override
    public boolean estaActiva(String nombreFlag, String userId) {
        if (!inicializado) {
            return false;
        }
        try {
            return Statsig.checkGateSync(new StatsigUser(userId), nombreFlag);
        } catch (Exception ex) {
            log.warn("No se pudo evaluar el flag {}", nombreFlag, ex);
            return false;
        }
    }

    @PreDestroy
    public void cerrar() {
        if (inicializado) {
            Statsig.shutdown();
        }
    }
}
