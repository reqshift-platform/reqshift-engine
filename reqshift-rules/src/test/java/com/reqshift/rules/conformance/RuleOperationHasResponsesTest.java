package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

class RuleOperationHasResponsesTest {

    private final RuleOperationHasResponses rule = new RuleOperationHasResponses();

    @Test
    void passesWhenResponsesDeclared() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      responses:
                        '200': {description: ok}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsOperationWithoutResponses() {
        // swagger-parser-v3 rejects YAML with missing 'responses', so we build the model.
        OpenAPI api = new OpenAPI();
        Paths paths = new Paths();
        paths.put("/pets", new PathItem().get(new Operation()));
        api.setPaths(paths);

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("CONF008");
    }
}
