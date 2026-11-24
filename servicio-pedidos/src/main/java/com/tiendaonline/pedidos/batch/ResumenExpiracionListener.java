package com.tiendaonline.pedidos.batch;

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
            log.info("Resumen expiracion de pedidos: jobExecutionId={} estado={} leidos={} escritos={} saltados={} duracionMs={}",
                    jobExecution.getId(),
                    jobExecution.getStatus(),
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount(),
                    Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis());
        }
    }
}
