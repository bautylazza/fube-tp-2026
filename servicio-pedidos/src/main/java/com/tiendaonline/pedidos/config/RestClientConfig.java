package com.tiendaonline.pedidos.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration 
public class RestClientConfig {

    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(2);
    private static final Duration TIMEOUT_LECTURA = Duration.ofSeconds(5);

    @Bean
    public RestClient clientesRestClient(RestClient.Builder builder, @Value ("${servicios.clientes.url}") String url) {
        return builder.baseUrl(url).requestFactory(requestFactory()).build();
    }

    @Bean
    public RestClient inventarioRestClient(RestClient.Builder builder, @Value("${servicios.inventario.url}") String url) {
        return builder.baseUrl(url).requestFactory(requestFactory()).build();
    }

    private ClientHttpRequestFactory requestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT_CONEXION)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(TIMEOUT_LECTURA);
        return factory;
    }
}
