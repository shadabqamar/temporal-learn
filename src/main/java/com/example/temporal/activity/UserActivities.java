package com.example.temporal.activity;

import com.example.temporal.model.UserRegistrationRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activities for user validation and communication.
 * All I/O must live here — NEVER in Workflow code (Article III).
 */
@ActivityInterface
public interface UserActivities {

    @ActivityMethod
    void validateUser(UserRegistrationRequest request);

    @ActivityMethod
    void sendWelcomeEmail(String email, String name);
}
