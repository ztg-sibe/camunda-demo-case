package io.camunda.demo.camundacatfactfinder;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class LoadCatFactsWorker {
    private final static Logger LOG = LoggerFactory.getLogger(LoadCatFactsWorker.class);

    private final RestClient restClient = RestClient.builder().baseUrl("https://catfact.ninja/fact").build();

    @JobWorker(type = "load-cat-facts")
    public Map<String, Object> loadCatFacts(@Variable(name = "number_of_facts") Integer numberOfFacts) {
        LOG.info("Loading {} cat facts", numberOfFacts);

        if (numberOfFacts > 8) {
            // this triggers the specific exception path in the process
            throw new ZeebeBpmnError("CANNOT_COMPUTE", "too many facts", Map.of());
        }


        List<String> catFacts = Stream.iterate(0, i -> i + 1)
                .limit(numberOfFacts)
                .sequential()
                .map(_ ->
                        {
                            CatFact catFact = restClient.get().retrieve().toEntity(CatFact.class).getBody();
                            LOG.debug("Random cat fact: {}", catFact);
                            return catFact;
                        }
                )
                .filter(Objects::nonNull)
                .map(CatFact::fact)
                .toList();


        return Map.of("cat_facts", catFacts);
    }
}

record CatFact(String fact, int length) {

}