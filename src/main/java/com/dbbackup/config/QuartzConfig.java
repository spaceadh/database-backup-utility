package com.dbbackup.config;

import com.dbbackup.service.BackupLogService;
import com.dbbackup.service.BackupOrchestrator;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Configuration for Quartz scheduler
 */
@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final BackupOrchestrator backupOrchestrator;
    private final BackupLogService backupLogService;

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        
        // Add services to scheduler context so they can be accessed by jobs
        scheduler.getContext().put("backupOrchestrator", backupOrchestrator);
        scheduler.getContext().put("backupLogService", backupLogService);
        
        return scheduler;
    }
}
