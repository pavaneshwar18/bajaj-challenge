package com.bajajfinserv.hiringchallenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChallengeService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        log.info("Application started. Executing the challenge flow...");

        // Step 1: Generate the webhook by sending a POST request [cite: 50]
        WebhookResponse webhookDetails = generateWebhook();

        if (webhookDetails != null && webhookDetails.webhookUrl() != null) {
            log.info("Successfully received webhook URL: {}", webhookDetails.webhookUrl());
            log.info("Access Token received.");

            // Step 2: Define the SQL query solution for Question 1
            String sqlQuery = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE DAY(p.PAYMENT_TIME) <> 1 ORDER BY p.AMOUNT DESC LIMIT 1;";

            // Step 3: Submit the solution [cite: 48]
            submitSolution(webhookDetails.webhookUrl(), webhookDetails.accessToken(), sqlQuery);
        } else {
            log.error("Failed to generate webhook. Aborting submission.");
        }
    }

    private WebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA"; // [cite: 51]
        WebhookRequest requestBody = new WebhookRequest("John Doe", "REG12347", "john@example.com"); // [cite: 54, 55, 56]

        log.info("Sending POST request to generate webhook...");
        try {
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(url, requestBody, WebhookResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("Error generating webhook. Status code: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception occurred while generating webhook: {}", e.getMessage());
            return null;
        }
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        SolutionRequest solutionBody = new SolutionRequest(finalQuery); // [cite: 73]

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // [cite: 70]
        headers.set("Authorization", accessToken); // [cite: 60, 69]

        HttpEntity<SolutionRequest> requestEntity = new HttpEntity<>(solutionBody, headers);

        log.info("Submitting the final SQL query to: {}", webhookUrl);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, requestEntity, String.class);
            log.info("Submission response status: {}", response.getStatusCode());
            log.info("Submission response body: {}", response.getBody());
        } catch (Exception e) {
            log.error("Exception occurred during solution submission: {}", e.getMessage());
        }
    }
}