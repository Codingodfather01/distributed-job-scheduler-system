package com.CodinGodfather.Scheduler.Controller;

import com.CodinGodfather.Scheduler.DTO.CreateJobRequest;
import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Service.JobService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/jobs")
public class JobController
{
    private JobService jobService;
    public JobController(JobService jobService)
    {
        this.jobService=jobService;
    }
    @PostMapping
    public JobEntity create(@RequestBody CreateJobRequest request)
    {
        return jobService.createJob(request);
    }
    @GetMapping
    public List<JobEntity>getAllJobs()
    {
        return jobService.getAllJobs();
    }
    @GetMapping(value="/{jobId}")
    public JobEntity getJobById(@PathVariable Long jobId)
    {
        return jobService.getJobById(jobId);
    }
    @GetMapping(value="/{jobId}/executions")
    public List<JobExecutionEntity>getExceutionsByJobId(@PathVariable Long jobId)
    {
        return jobService.getExecutionsByJobId(jobId);
    }
    @GetMapping(value = "/dlq")
    public List<JobExecutionEntity> getStatusDLQ()
    {
        return jobService.getDLQStatus();
    }
    @PostMapping("/dlq/{executionId}/retry")
    public JobExecutionEntity manualRetryFromDLQ(@PathVariable Long executionId) {
        return jobService.manualRetryFromDLQ(executionId);
    }
}
