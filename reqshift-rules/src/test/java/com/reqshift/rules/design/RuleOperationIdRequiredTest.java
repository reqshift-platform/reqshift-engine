package com.reqshift.rules.design;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleOperationIdRequiredTest {

    private final RuleOperationIdRequired rule = new RuleOperationIdRequired();

    @Test
    void flagsOperationWithoutOperationId() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      responses: {'200': {description: ok}}
                """);

        List<Violation> violations = rule.check(api);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("DES001");
        assertThat(violations.get(0).severity()).isEqualTo(Severity.WARNING);
        assertThat(violations.get(0).message()).contains("/pets");
    }

    @Test
    void passesWhenOperationIdIsPresent() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      operationId: listPets
                      responses: {'200': {description: ok}}
                """);

        assertThat(rule.check(api)).isEmpty();
    }
}
