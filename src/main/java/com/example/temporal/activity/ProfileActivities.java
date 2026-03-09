package com.example.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/** Activities for profile creation. Run inside the Child Workflow. */
@ActivityInterface
public interface ProfileActivities {

    @ActivityMethod
    void createUserProfile(String userId, String department);

    @ActivityMethod
    void assignDefaultRole(String userId);
}
