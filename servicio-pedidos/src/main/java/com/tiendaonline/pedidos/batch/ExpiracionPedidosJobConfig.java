package com.tiendaonline.pedidos.batch;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import com.tiendaonline.pedidos.entity.EstadoPedido;
import com.tiendaonline.pedidos.exception.ServicioNoDisponibleException;

@Configuration
@EnableScheduling
public class ExpiracionPedidosJobConfig {

    public static final String PARAMETRO_FECHA_CORTE = "fechaCorte";

    private static final int TAMANIO_CHUNK = 10;
    private static final int LIMITE_SALTADOS = 100;

    @Bean
    public Job expiracionPedidosJob(JobRepository jobRepository, Step expiracionPedidosStep,
            ResumenExpiracionListener resumenListener) {
        return new JobBuilder("expiracionPedidosJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(resumenListener)
                .start(expiracionPedidosStep)
                .build();
    }

    @Bean
    public Step expiracionPedidosStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<PedidoVencido> pedidosVencidosReader,
            ExpiracionPedidoProcessor processor, ExpiracionPedidoWriter writer) {
        return new StepBuilder("expiracionPedidosStep", jobRepository)
                .<PedidoVencido, PedidoExpirado>chunk(TAMANIO_CHUNK, transactionManager)
                .reader(pedidosVencidosReader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(ServicioNoDisponibleException.class)
                .skipLimit(LIMITE_SALTADOS)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<PedidoVencido> pedidosVencidosReader(DataSource dataSource,
            @Value("#{jobParameters['" + PARAMETRO_FECHA_CORTE + "']}") LocalDateTime fechaCorte) {
        return new JdbcCursorItemReaderBuilder<PedidoVencido>()
                .name("pedidosVencidosReader")
                .dataSource(dataSource)
                .sql("SELECT id, estado FROM pedidos WHERE estado = 'CREADO' AND fecha_creacion < ? ORDER BY fecha_creacion, id")
                .preparedStatementSetter(ps -> ps.setObject(1, fechaCorte))
                .rowMapper((rs, fila) -> new PedidoVencido(
                        rs.getObject("id", UUID.class),
                        EstadoPedido.valueOf(rs.getString("estado"))))
                .build();
    }
}
