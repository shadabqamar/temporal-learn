package com.example.temporal.workflow;

import com.example.temporal.model.ProfileSetupRequest;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Child Workflow that handles profile creation independently.
 * Justified per Article V: own event history; could run on a separate Worker set.
 * Receives department via @SignalMethod after being started by the parent.
 */
@WorkflowInterface
public interface ProfileSetupChildWorkflow {

    @WorkflowMethod
    String setupProfile(ProfileSetupRequest request);

    /**
     * Signal sent by the parent workflow after child starts.
     * Child blocks with Workflow.await() until this arrives.
     */
    @SignalMethod
    void setDepartment(String department);
}
