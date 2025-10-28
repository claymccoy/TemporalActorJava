package com.claymccoy.meteor.shower.temporal;

import io.temporal.client.WorkflowClient;

public record TemporalConfig(WorkflowClient client, String taskQueue) {}
