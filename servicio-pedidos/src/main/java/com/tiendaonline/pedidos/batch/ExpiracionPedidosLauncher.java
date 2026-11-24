package com.tiendaonline.pedidos.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiracionPedidosLauncher {

    private static final int HORAS_PARA_EXPIRAR = 24;

    private final Job expiracionPedidosJob;
    private final JobExplorer jobExplorer;
    private final TaskExecutorJobLauncher jobLauncher;

    // El JobLauncher de Spring Boot es sincronico. Se arma uno propio que no bloquea
    // para que el endpoint pueda responder 202 sin esperar a que termine la corrida.
    public ExpiracionPedidosLauncher(Job expiracionPedidosJob, JobExplorer jobExplorer, JobRepository jobRepository)
            throws Exception {
        this.expiracionPedidosJob = expiracionPedidosJob;
        this.jobExplorer = jobExplorer;
        this.jobLauncher = new TaskExecutorJobLauncher();
        this.jobLauncher.setJobRepository(jobRepository);
        this.jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor("expiracion-"));
        this.jobLauncher.afterPropertiesSet();
    }

    @Scheduled(cron = "${expiracion-pedidos.cron}")
    public void lanzarProgramado() throws JobExecutionException {
        lanzar();
    }

    public JobExecution lanzar() throws JobExecutionException {
        JobParameters parametros = new JobParametersBuilder(jobExplorer)
                .getNextJobParameters(expiracionPedidosJob)
                .addLocalDateTime(ExpiracionPedidosJobConfig.PARAMETRO_FECHA_CORTE,
                        LocalDateTime.now().minusHours(HORAS_PARA_EXPIRAR))
                .toJobParameters();

        return jobLauncher.run(expiracionPedidosJob, parametros);
    }
}
