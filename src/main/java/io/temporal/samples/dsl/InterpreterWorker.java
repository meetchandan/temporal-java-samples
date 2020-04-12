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

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterpreterWorker {
  private static final String TASK_LIST = "Interpreter";

  public static void main(String[] args) {

    // Start a worker that hosts both workflow and activity implementations.
    // gRPC stubs wrapper that talks to the local docker instance of temporal service.
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
    // client that can be used to start and signal workflows
    WorkflowClient client = WorkflowClient.newInstance(service);

    // worker factory that can be used to create workers for specific task lists
    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(TASK_LIST);

    Map<String, Map<String, String>> definitions = new HashMap<>();
    Map<String, String> definition1 = getDefinition1();
    definitions.put("type1", definition1);

    // Workflows are stateful. So you need a type to create instances.
    worker.registerWorkflowImplementationTypes(InterpreterWorkflowImpl.class);
    // Activities are stateless and thread safe. So a shared instance is used.
    worker.registerActivitiesImplementations(new SequenceInterpreter(definitions));
    // Start listening to the workflow and activity task lists.
    factory.start();

    InterpreterWorkflow workflow = client.newWorkflowStub(InterpreterWorkflow.class);
    // Execute a workflow waiting for it to complete.

    String greeting = workflow.execute("type1", "input1");
    System.out.println(greeting);
    System.exit(0);
  }

  private static Map<String, String> getDefinition1() {
    Map<String, String> definition1 = new HashMap<>();
    List<String> sequence =
        Arrays.asList(
            "Activities::activity1",
            "Activities::activity2",
            "Activities::activity3",
            "Activities::activity4"
            );
    definition1.put("init", sequence.get(0));

    for (int i = 0; i < sequence.size() - 1; i++) {
      definition1.put(sequence.get(i), sequence.get(i + 1));
    }
    definition1.put(sequence.get(sequence.size() - 1), null);
    return definition1;
  }
}
