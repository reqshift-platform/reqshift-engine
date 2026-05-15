package com.reqshift.rules.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleInfoDescriptionRequiredTest {

    private final RuleInfoDescriptionRequired rule = new RuleInfoDescriptionRequired();

    @Test
    void flagsMissingDescription() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                """);

        List<Violation> violations = rule.check(api);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("DOC001");
        assertThat(violations.get(0).location()).isEqualTo("#/info/description");
    }

    @Test
    void passesWhenDescriptionPresent() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info:
                  title: T
                  version: 1.0.0
                  description: A nice and helpful API description.
                paths: {}
                """);

        assertThat(rule.check(api)).isEmpty();
    }
}
