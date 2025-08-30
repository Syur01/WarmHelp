package com.warmhelp.app.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
@Service
public class EmailService implements IEmailService {

    private final String apiKey = System.getenv("BREVO_API_KEY");
    private final String fromEmail = System.getenv("FROM_EMAIL");
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendEmail(String[] toEmails, String subject, String htmlContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail));

        List<Map<String, String>> toList = new ArrayList<>();
        for (String to : toEmails) {
            toList.add(Map.of("email", to));
        }
        body.put("to", toList);
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    @Override
    public void sendEmailWithFile(String[] toUser, String subject, String message, File file) {

    }
}

