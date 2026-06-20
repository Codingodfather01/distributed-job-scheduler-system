package com.CodinGodfather.Scheduler.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/worker")
public class WorkerSimulationController {

    private static final Logger log =
            LoggerFactory.getLogger(WorkerSimulationController.class);

    @PostMapping("/process")
    public String processJob() {

        log.info("Worker received job");

        return "Job processed successfully";
    }
}