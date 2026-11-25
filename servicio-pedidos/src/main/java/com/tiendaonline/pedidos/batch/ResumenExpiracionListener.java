package com.tiendaonline.pedidos.batch;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.Duration;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResumenExpiracionListener implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        for (StepExecution step : jobExecution.getStepExecutions()) {
            log.info("Resumen expiracion de pedidos: {} {} {} {} {} {}",
                    kv("jobExecutionId", jobExecution.getId()),
                    kv("estado", jobExecution.getStatus()),
                    kv("leidos", step.getReadCount()),
                    kv("escritos", step.getWriteCount()),
                    kv("saltados", step.getSkipCount()),
                    kv("duracionMs", Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis()));
        }
    }
}
