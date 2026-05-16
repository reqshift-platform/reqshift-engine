package com.reqshift.rules.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;

class RuleArrayHasItemsTest {

    private final RuleArrayHasItems rule = new RuleArrayHasItems();

    @Test
    void passesWithItems() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  schemas:
                    Tags:
                      type: array
                      items: {type: string}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsArrayWithoutItems() {
        // swagger-parser may inject a default items, so we build the model.
        OpenAPI api = new OpenAPI();
        ArraySchema arr = new ArraySchema();
        arr.setItems(null);
        api.setComponents(new Components().addSchemas("Tags", arr));

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("SCHEMAS003");
    }
}
