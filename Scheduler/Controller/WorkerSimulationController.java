package com.CodinGodfather.Scheduler.Controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/worker")
public class WorkerSimulationController {

    @PostMapping("/process")
    public String processJob() {

        System.out.println("Worker received job");

        return "Job processed successfully";
    }
}