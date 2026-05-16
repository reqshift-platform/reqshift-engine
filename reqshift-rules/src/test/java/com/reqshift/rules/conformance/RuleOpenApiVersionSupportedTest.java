package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Severity;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleOpenApiVersionSupportedTest {

    private final RuleOpenApiVersionSupported rule = new RuleOpenApiVersionSupported();

    @Test
    void passesOnSupportedVersion() {
        OpenAPI api =
                load(
                        """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMissingVersion() {
        // swagger-models defaults openapi to "3.0.1" in the no-arg constructor, so we nullify it.
        OpenAPI api = new OpenAPI();
        api.setOpenapi(null);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(
                        v -> {
                            assertThat(v.ruleId()).isEqualTo("CONF001");
                            assertThat(v.severity()).isEqualTo(Severity.ERROR);
                        });
    }

    private static OpenAPI load(String yaml) {
        return new OpenApiLoader().loadFromString(yaml);
    }
}
