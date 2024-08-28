package io.camunda.demo.camundacatfactfinder;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import io.camunda.zeebe.spring.test.ZeebeTestThreadSupport;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ZeebeSpringTest
@Deployment(resources = {"classpath:cat_process.bpmn", "classpath:form_1.form", "classpath:form_2.form"})
@ExtendWith(ProcessEngineCoverageExtension.class)
public class CatFactProcessTest {

    @Autowired
    private ZeebeClient client;

    @Autowired
    private ZeebeTestEngine engine;

    @Test
    void testFailTooMany() throws Exception {
        // trigger the process
        ProcessInstanceEvent job = client.newCreateInstanceCommand()
                .bpmnProcessId("Process_151e385")
                .latestVersion()
                .send()
                .join();

        // fill out the input form in step one
        waitForUserTaskAndComplete("Activity_0gt4i06", Map.of("number_of_facts", 9));
        // it is important to wait here, otherwise the engine will be killed
        ZeebeTestThreadSupport.waitForProcessInstanceCompleted(job);
        BpmnAssert.assertThat(job).isCompleted();
    }

    @Test
    void testSuccessMultiple() throws Exception {
        // trigger the process
        ProcessInstanceEvent job = client.newCreateInstanceCommand()
                .bpmnProcessId("Process_151e385")
                .latestVersion()
                .send()
                .join();
        // fill the input form
        waitForUserTaskAndComplete("Activity_0gt4i06", Map.of("number_of_facts", 5));
        // now wait until the next step of the process is completed.
        // this is important, otherwise the next step will fail
        ZeebeTestThreadSupport.waitForProcessInstanceHasPassedElement(job, "Activity_142j5pj");
        // "confirm" the display of the cat facts
        // TODO how to test that the cat facts are actually displayed?
        waitForUserTaskAndComplete("Activity_00ace6x", null);

        //wait until finished and do final assertions
        ZeebeTestThreadSupport.waitForProcessInstanceCompleted(job);
        BpmnAssert.assertThat(job).isCompleted();
    }

    public void waitForUserTaskAndComplete(String userTaskId, Map<String, Object> variables) throws Exception {
        // Let the workflow engine do whatever it needs to do
        engine.waitForIdleState(Duration.of(5, ChronoUnit.SECONDS));

        // Now get all user tasks
        List<ActivatedJob> jobs = client
                .newActivateJobsCommand()
                .jobType(USER_TASK_JOB_TYPE)
                .maxJobsToActivate(1)
                .send()
                .join()
                .getJobs();

        // Should be only one
        assertTrue(jobs.size() > 0, "Job for user task '" + userTaskId + "' does not exist");
        ActivatedJob userTaskJob = jobs.get(0);
        // Make sure it is the right one
        if (userTaskId != null) {
            assertEquals(userTaskId, userTaskJob.getElementId());
        }

        // And complete it passing the variables
        if (variables != null) {
            client.newCompleteCommand(userTaskJob.getKey()).variables(variables).send().join();
        } else {
            client.newCompleteCommand(userTaskJob.getKey()).send().join();
        }

    }
}
