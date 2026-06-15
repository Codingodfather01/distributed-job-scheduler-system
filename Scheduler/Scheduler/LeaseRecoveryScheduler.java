package com.CodinGodfather.Scheduler.Scheduler;

import com.CodinGodfather.Scheduler.Service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LeaseRecoveryScheduler {

    private final JobService jobService;

    public LeaseRecoveryScheduler(JobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(fixedRate = 10000)
    public void recoverExpiredLeases() {
        jobService.recoverExpiredLeases();
    }
}