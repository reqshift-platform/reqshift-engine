package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

class RulePathStartsWithSlashTest {

    private final RulePathStartsWithSlash rule = new RulePathStartsWithSlash();

    @Test
    void passesWhenAllPathsStartWithSlash() {
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
                  /orders:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsPathWithoutLeadingSlash() {
        // YAML/JSON parsers may auto-fix this, so we build the model directly.
        OpenAPI api = new OpenAPI();
        Paths paths = new Paths();
        paths.put("pets", new PathItem());
        api.setPaths(paths);

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("CONF007");
        assertThat(violations.get(0).message()).contains("pets");
    }
}
