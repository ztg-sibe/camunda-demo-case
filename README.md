# How to Camunda

## Getting started

You can either use a local setup or the SAAS platform provided by Camunda. You can get a 30 day evaluation trial. The biggest difference I could see so far is that the SAAS platform has built-in connectors (e.g REST, RabbitMQ, ...) that can be used out of the box in the process diagram. The local installation does not ship with these, but it seems with some effort they can be installed locally as well (untested).

If you prefer to work with code only, you don't necessarily need the whole setup, as you can run and test your processes with an embedded engine using spring as well. However, to get familiar with how camunda works, I would **recommend to use the local setup**.

### Local setup

#### Start the platform
To run your camunda instance locally, checkout the official platform repository or download the compose files from git:
`https://github.com/camunda/camunda-platform.git`

The [docker-compose-core.yaml](https://github.com/camunda/camunda-platform/blob/main/docker-compose-core.yaml) contains all necessary services for local testing but omits Authentication server and some other stuff I didn't miss. 

Run `docker compose -f docker-compose-core.yaml up -d` to start all services. To work with the platform, you will need the following services:

* Operate at http://localhost:8081/login
* Tasklist at http://localhost:8082/login
* Cluster endpoint at http://localhost:26500 (not reachable via browser)

**Username** `demo`, **password** `demo`

#### Setup Camunda Modeler

Download [Open Source Desktop Modeler](https://camunda.com/download/modeler/) for your platform and execute it.
Create a new BPMN Diagram and click the deployment rocket in the footer. Here you configure the **Camunda 8 Self-Managed** endpoint to `http://localhost:26500` with **no authentication.**
![Configure self-managed instance](https://docs.camunda.io/assets/images/deploy-empty-24ac8590e28b044747fd13a173273eaf.png)
Clicking **Deploy** should be successful now.
### SAAS Setup

Simply [create an account](https://docs.camunda.io/docs/guides/getting-started/), open the [Camunda Console](https://console.camunda.io/) and [model your first process](https://docs.camunda.io/docs/guides/model-your-first-process/).

**Note that this example will only work with the local cluster out of the box.** If you want to connect the service to the cloud platform, you need to adapt the properties with the corresponding URLs and authentication keys at `src/main/resources/application.properties`.

## Work with this example
This example contains a simple process with a user input (select number of cat facts that you wish to request) and a microservice that takes the number as input parameter and calls a Rest API to get X random cat facts. They are then returned and showed in a Form to be consumed by a user.

### Deploy the diagrams and run the application

You will find the relevant Camunda files in `src/main/resources`. Per default, the spring application only runs the 
microservice and you have to take care of deploying the diagrams manually. I recommend doing this first, to understand how
it is working. However, if you enable this line in the spring application,
```java
@Deployment(resources = {"classpath:deploy/*.bpmn","classpath:deploy/*.form"})
```
the diagrams are automatically deployed on start. This is very useful for development as you always get the latest version of your workflow.



* Open the `.bpmn` and both `.form` files in the desktop modeller and deploy them. **You need to deploy each file**.
* Start the process by **either**
  * press the RUN arrow next to deployment of the `.bpmn` file **OR**
  * open your local Tasklist instance, go to Processed and start the process there.
* Check your local Operate instance, go to Processes, open the only active process
  * the Process should be waiting in the first step (user input)
* Whenever user input is required, you will find the Task in the Tasklist instance
  * open it, assign it to yourself
  * fill the Form and confirm it
* Check Operate, it should now wait in the **Load cat facts** step, as the corresponding service is offline.
* To start the microservice, simply run the main method in this project at `io.camunda.demo.camundacatfactfinder.CamundaCatFactFinderApplication`
  * it should automatically register at your local Camunda instance (see `application.properties`)
  * see expected log output below. Ignore the Spring Warnings
* The process should proceed to the next user input.
  * Go to Tasklist, select the available Task and enjoy your free cat facts.
  * Assign it and complete the Task to finish the process.

**Sample log of a successful run:**
```
2024-08-28T10:21:13.609+02:00  INFO 49080 --- [camunda-cat-fact-finder] [           main] z.s.c.a.p.ZeebeWorkerAnnotationProcessor : Configuring 1 Zeebe worker(s) of bean 'loadCatFactsWorker': [ZeebeWorkerValue{type='load-cat-facts', name='', timeout=PT-0.001S, maxJobsActive=-1, requestTimeout=PT-1S, pollInterval=PT-0.001S, autoComplete=true, fetchVariables=[], enabled=true, methodInfo=io.camunda.zeebe.spring.client.bean.MethodInfo@1cd3b138, tenantIds=[], forceFetchAllVariables=false, streamEnabled=false, streamTimeout=PT-0.001S}]
2024-08-28T10:21:13.656+02:00 ERROR 49080 --- [camunda-cat-fact-finder] [           main] i.c.c.auth.DefaultNoopAuthentication     : Unable to determine authentication. Please check your configuration
2024-08-28T10:21:13.671+02:00  INFO 49080 --- [camunda-cat-fact-finder] [           main] z.s.c.c.ZeebeClientProdAutoConfiguration : Creating ZeebeClient using ZeebeClientConfiguration [ZeebeClientConfiguration{properties=ZeebeClientConfigurationProperties{environment=ApplicationEnvironment {activeProfiles=[], defaultProfiles=[default], propertySources=[ConfigurationPropertySourcesPropertySource {name='configurationProperties'}, PropertiesPropertySource {name='systemProperties'}, OriginAwareSystemEnvironmentPropertySource {name='systemEnvironment'}, RandomValuePropertySource {name='random'}, OriginTrackedMapPropertySource {name='Config resource 'class path resource [application.properties]' via location 'optional:classpath:/''}]}, connectionMode='null', defaultTenantId='<default>', defaultJobWorkerTenantIds=[<default>], applyEnvironmentVariableOverrides=false, enabled=true, broker=Broker{gatewayAddress='null', keepAlive=PT45S}, cloud=Cloud{clusterId='null', clientId='***', clientSecret='***', region='bru-2', scope='null', baseUrl='zeebe.camunda.io', authUrl='https://login.cloud.camunda.io/oauth/token', port=443, credentialsCachePath='null'}, worker=Worker{maxJobsActive=32, threads=1, defaultName='null', defaultType='null', override={}}, message=Message{timeToLive=PT1H, maxMessageSize=4194304}, security=Security{plaintext=true, overrideAuthority='null', certPath='null'}, job=Job{timeout=PT5M, pollInterval=PT0.1S}, ownsJobWorkerExecutor=false, defaultJobWorkerStreamEnabled=false, requestTimeout=PT10S}, camundaClientProperties=io.camunda.zeebe.spring.client.properties.CamundaClientProperties@617389a, authentication=io.camunda.common.auth.DefaultNoopAuthentication@1c8f6a90, jsonMapper=io.camunda.zeebe.client.impl.ZeebeObjectMapper@3050ac2f, interceptors=[], zeebeClientExecutorService=io.camunda.zeebe.spring.client.jobhandling.ZeebeClientExecutorService@265bd546}]
2024-08-28T10:21:14.136+02:00  INFO 49080 --- [camunda-cat-fact-finder] [           main] i.c.z.s.c.jobhandling.JobWorkerManager   : . Starting Zeebe worker: ZeebeWorkerValue{type='load-cat-facts', name='loadCatFactsWorker#chargeCreditCard', timeout=PT-0.001S, maxJobsActive=-1, requestTimeout=PT-1S, pollInterval=PT-0.001S, autoComplete=true, fetchVariables=[number_of_facts], enabled=true, methodInfo=io.camunda.zeebe.spring.client.bean.MethodInfo@1cd3b138, tenantIds=[], forceFetchAllVariables=false, streamEnabled=false, streamTimeout=PT-0.001S}
2024-08-28T10:21:14.143+02:00  INFO 49080 --- [camunda-cat-fact-finder] [           main] i.c.d.c.CamundaCatFactFinderApplication  : Started CamundaCatFactFinderApplication in 1.826 seconds (process running for 2.518)
2024-08-28T10:21:14.496+02:00  INFO 49080 --- [camunda-cat-fact-finder] [pool-2-thread-1] i.c.d.c.LoadCatFactsWorker               : Loading 3 cat facts
```



### Run the test

There is a "process test" that is running the complete business process in an embedded engine at `io.camunda.demo.camundacatfactfinder.CatFactProcessTest`. This test can be executed stand-alone from within the IDE. 
After execution, run the report at `target/process-test-coverage/io.camunda.demo.camundacatfactfinder.CatFactProcessTest/report.html` to investigate the test coverage.

## Other Examples and resources
* [Camunda Examples](https://github.com/camunda-community-hub/camunda-8-examples)
  * https://github.com/camunda-community-hub/camunda-8-examples/tree/main/twitter-review-java-springboot
* [Zeebe process test](https://github.com/camunda/zeebe-process-test)
  * https://github.com/camunda/zeebe-process-test/blob/main/examples/src/test/java/io/camunda/zeebe/process/test/examples/PullRequestProcessTest.java
* Useful documentation links
  * [Test processes](https://docs.camunda.io/docs/components/best-practices/development/testing-process-definitions/)
  * [Exception Handling](https://docs.camunda.io/docs/components/best-practices/development/dealing-with-problems-and-exceptions/)
* [Camunda Connectors](https://github.com/camunda/connectors)
  * [Rest Connector](https://github.com/camunda/connectors/tree/main/connectors/http/rest/element-templates)
  * [Custom connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-templates/)