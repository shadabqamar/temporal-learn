package com.example.temporal.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileActivitiesImpl implements ProfileActivities {

    private static final Logger log = LoggerFactory.getLogger(ProfileActivitiesImpl.class);

    @Override
    public void createUserProfile(String userId, String department) {
        log.info("Creating profile for userId={} in department={}", userId, department);
        // Simulated: in production, write to DB here
        log.info("Profile created for userId={}", userId);
    }

    @Override
    public void assignDefaultRole(String userId) {
        log.info("Assigning default role to userId={}", userId);
        // Simulated: in production, call RBAC service here
        log.info("Default role assigned to userId={}", userId);
    }
}
