package com.tiendaonline.pedidos.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.entity.Pedido;
import com.tiendaonline.pedidos.repository.PedidoRepository;

@SpringBatchTest
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ExpiracionPedidosJobTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer(0);

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final List<UUID> creados = new ArrayList<>();
    private JobExecution ejecucion;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.inventario.url", () -> "http://localhost:" + mockServer.getLocalPort());
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }

    @AfterEach
    void borrarDatos() {
        pedidoRepository.deleteAllById(creados);
        if (ejecucion != null) {
            jobRepositoryTestUtils.removeJobExecutions(List.of(ejecucion));
        }
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }

    @Test
    void expiraSoloLosCreadosDeMasDe24HorasYLiberaSuStock(CapturedOutput salida) throws Exception {
        UUID vencido = insertar(EstadoPedido.CREADO, 25);
        UUID reciente = insertar(EstadoPedido.CREADO, 1);
        UUID confirmado = insertar(EstadoPedido.CONFIRMADO, 25);
        mockServer.when(request().withMethod("PATCH")).respond(response().withStatusCode(200));

        ejecutarJob();

        assertThat(ejecucion.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(estadoDe(vencido)).isEqualTo(EstadoPedido.EXPIRADO);
        assertThat(estadoDe(reciente)).isEqualTo(EstadoPedido.CREADO);
        assertThat(estadoDe(confirmado)).isEqualTo(EstadoPedido.CONFIRMADO);
        mockServer.verify(liberacionDe(vencido), VerificationTimes.once());
        mockServer.verify(liberacionDe(reciente), VerificationTimes.exactly(0));
        mockServer.verify(liberacionDe(confirmado), VerificationTimes.exactly(0));
        assertThat(salida).contains("Resumen expiracion de pedidos: jobExecutionId=" + ejecucion.getId());
    }

    @Test
    void siInventarioFallaConUnPedidoLoSaltaYExpiraLosDemas() throws Exception {
        UUID primero = insertar(EstadoPedido.CREADO, 27);
        UUID conFalla = insertar(EstadoPedido.CREADO, 26);
        UUID ultimo = insertar(EstadoPedido.CREADO, 25);
        mockServer.when(liberacionDe(conFalla)).respond(response().withStatusCode(500));
        mockServer.when(request().withMethod("PATCH")).respond(response().withStatusCode(200));

        ejecutarJob();

        assertThat(ejecucion.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(estadoDe(primero)).isEqualTo(EstadoPedido.EXPIRADO);
        assertThat(estadoDe(conFalla)).isEqualTo(EstadoPedido.CREADO);
        assertThat(estadoDe(ultimo)).isEqualTo(EstadoPedido.EXPIRADO);
        mockServer.verify(liberacionDe(ultimo), VerificationTimes.atLeast(1));
        StepExecution step = ejecucion.getStepExecutions().iterator().next();
        assertThat(step.getSkipCount()).isEqualTo(1);
    }

    private void ejecutarJob() throws Exception {
        ejecucion = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addLocalDateTime(ExpiracionPedidosJobConfig.PARAMETRO_FECHA_CORTE, LocalDateTime.now().minusHours(24))
                .toJobParameters());
    }

    private UUID insertar(EstadoPedido estado, int horasAtras) {
        LocalDateTime fecha = LocalDateTime.now().minusHours(horasAtras);
        UUID id = pedidoRepository.save(new Pedido(null, UUID.randomUUID(), new ArrayList<>(), estado,
                BigDecimal.ZERO, fecha, fecha)).getId();
        creados.add(id);
        return id;
    }

    private EstadoPedido estadoDe(UUID id) {
        return pedidoRepository.findById(id).orElseThrow().getEstado();
    }

    private HttpRequest liberacionDe(UUID pedidoId) {
        return request().withMethod("PATCH").withPath("/api/v1/reservas")
                .withQueryStringParameter("pedidoId", pedidoId.toString());
    }
}
