package com.reqshift.rules.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleTagHasDescriptionTest {

    private final RuleTagHasDescription rule = new RuleTagHasDescription();

    @Test
    void passesWithTagDescriptions() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                tags:
                  - name: pets
                    description: Operations on pet resources.
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsTagWithoutDescription() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                tags:
                  - name: pets
                paths: {}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("DOC008");
    }

    @Test
    void skipsApiWithoutGlobalTags() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }
}
