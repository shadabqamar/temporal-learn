package com.example.temporal.activity;

import com.example.temporal.model.UserRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of UserActivities. Uses SLF4J — NOT Workflow.getLogger() (Article IX). */
public class UserActivitiesImpl implements UserActivities {

    private static final Logger log = LoggerFactory.getLogger(UserActivitiesImpl.class);

    @Override
    public void validateUser(UserRegistrationRequest request) {
        log.info("Validating user: userId={}, email={}", request.getUserId(), request.getEmail());

        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + request.getEmail());
        }

        log.info("User validation passed for userId={}", request.getUserId());
    }

    @Override
    public void sendWelcomeEmail(String email, String name) {
        log.info("Sending welcome email to {} <{}>", name, email);
        // Simulated: in production, call an email service here
        log.info("Welcome email sent to {}", email);
    }
}
