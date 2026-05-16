package com.reqshift.rules.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleParameterHasDescriptionTest {

    private final RuleParameterHasDescription rule = new RuleParameterHasDescription();

    @Test
    void passesWithDescription() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets/{petId}:
                    get:
                      parameters:
                        - name: petId
                          in: path
                          required: true
                          description: Unique identifier of the pet.
                          schema: {type: string}
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMissingDescription() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets/{petId}:
                    get:
                      parameters:
                        - {name: petId, in: path, required: true, schema: {type: string}}
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("DOC005");
    }
}
