package com.dbbackup.service;

import com.dbbackup.model.BackupConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

/**
 * Service for scheduling automatic backups using Quartz
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {

    private final Scheduler scheduler;
    private final BackupOrchestrator backupOrchestrator;
    private final BackupLogService logService;

    /**
     * Schedules a recurring backup job
     *
     * @param config         the backup configuration
     * @param cronExpression the cron expression for scheduling (e.g., "0 0 2 * * ?" for daily at 2 AM)
     * @throws SchedulerException if scheduling fails
     */
    public void scheduleBackup(BackupConfig config, String cronExpression) throws SchedulerException {
        log.info("Scheduling backup for database: {} with cron: {}", config.getDatabaseName(), cronExpression);

        // Create job detail
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("config", config);

        JobDetail jobDetail = JobBuilder.newJob(BackupJob.class)
                .withIdentity("backup-job-" + config.getDatabaseName(), "backup-jobs")
                .setJobData(jobDataMap)
                .build();

        // Create trigger with cron expression
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("backup-trigger-" + config.getDatabaseName(), "backup-triggers")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        // Schedule the job
        scheduler.scheduleJob(jobDetail, trigger);

        log.info("Backup scheduled successfully for database: {}", config.getDatabaseName());
    }

    /**
     * Cancels a scheduled backup job
     *
     * @param databaseName the name of the database
     * @throws SchedulerException if cancellation fails
     */
    public void cancelScheduledBackup(String databaseName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("backup-job-" + databaseName, "backup-jobs");
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            log.info("Cancelled scheduled backup for database: {}", databaseName);
        } else {
            log.warn("No scheduled backup found for database: {}", databaseName);
        }
    }

    /**
     * Lists all scheduled backup jobs
     *
     * @throws SchedulerException if listing fails
     */
    public void listScheduledBackups() throws SchedulerException {
        log.info("Listing all scheduled backups:");
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                log.info("Job: {}", jobKey.getName());
            }
        }
    }

    /**
     * Quartz Job implementation for executing backups
     */
    @RequiredArgsConstructor
    public static class BackupJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            BackupConfig config = (BackupConfig) dataMap.get("config");

            // Get services from application context
            try {
                SchedulerContext schedulerContext = context.getScheduler().getContext();
                BackupOrchestrator orchestrator = (BackupOrchestrator) schedulerContext.get("backupOrchestrator");
                BackupLogService logService = (BackupLogService) schedulerContext.get("backupLogService");

                if (orchestrator != null) {
                    logService.logScheduledBackup(context.getTrigger().getKey().getName());
                    orchestrator.executeBackup(config);
                }
            } catch (SchedulerException e) {
                throw new JobExecutionException("Failed to execute backup job", e);
            }
        }
    }
}
