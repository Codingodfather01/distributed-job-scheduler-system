package com.CodinGodfather.Scheduler.Service;

import com.CodinGodfather.Scheduler.DTO.JobCallbackRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpCallbackService {

    private final RestTemplate restTemplate;

    public HttpCallbackService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean executeCallback(
            String callbackUrl,
            Long jobId,
            Long executionId) {

        JobCallbackRequest request =
                new JobCallbackRequest();

        request.setJobId(jobId);
        request.setExecutionId(executionId);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        callbackUrl,
                        request,
                        String.class
                );

        return response.getStatusCode().is2xxSuccessful();
    }
}