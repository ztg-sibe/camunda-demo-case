package io.camunda.demo.camundacatfactfinder;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// OPTIONAL:
// Deploy all resources to your local camunda instance on start
//@Deployment(resources = {"classpath:deploy/*.bpmn","classpath:deploy/*.form"})
public class CamundaCatFactFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaCatFactFinderApplication.class, args);
    }

}
