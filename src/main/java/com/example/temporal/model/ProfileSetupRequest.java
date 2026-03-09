package com.example.temporal.model;

/** Input to ProfileSetupChildWorkflow @WorkflowMethod. Department is set later via @SignalMethod. */
public class ProfileSetupRequest {

    private String userId;
    private String name;
    private String department; // set after child start via setDepartment signal

    public ProfileSetupRequest() {}

    public ProfileSetupRequest(String userId, String name) {
        this.userId = userId;
        this.name   = name;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
