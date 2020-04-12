/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.samples.dsl;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.ActivityStub;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class InterpreterWorkflowImpl implements InterpreterWorkflow {

  private final Interpreter interpreter =
      Workflow.newActivityStub(
          Interpreter.class,
          ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofMinutes(10)).build());

  private final ActivityStub activities =
      Workflow.newUntypedActivityStub(
          ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofMinutes(10)).build());

  private String currentActivity = "init";
  private String lastActivityResult;

  @Override
  public String execute(String workflowType, String input) {
    do {
      currentActivity = interpreter.getNextStep(workflowType, currentActivity);
      lastActivityResult = activities.execute(currentActivity, String.class, lastActivityResult);
    } while (currentActivity != null);
    return lastActivityResult;
  }

  @Override
  public String getCurrentActivity() {
    return currentActivity;
  }
}
