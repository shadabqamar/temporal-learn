package com.example.temporal.model;

/** Return value of UserOnboardingWorkflow @WorkflowMethod. */
public class OnboardingResult {

    private String userId;
    private OnboardingStatus finalStatus;
    private String message;

    public OnboardingResult() {}

    public OnboardingResult(String userId, OnboardingStatus finalStatus, String message) {
        this.userId      = userId;
        this.finalStatus = finalStatus;
        this.message     = message;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OnboardingStatus getFinalStatus() { return finalStatus; }
    public void setFinalStatus(OnboardingStatus finalStatus) { this.finalStatus = finalStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "OnboardingResult{userId='" + userId + "', status=" + finalStatus + ", message='" + message + "'}";
    }
}
