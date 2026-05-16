package com.reqshift.rules.design;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class RuleNoTrailingSlashTest {

    private final RuleNoTrailingSlash rule = new RuleNoTrailingSlash();

    @Test
    void passesWithoutTrailingSlash() {
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
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsTrailingSlash() {
        // swagger-parser may normalise trailing slashes, so we build the model.
        OpenAPI api = new OpenAPI();
        Operation op = new Operation();
        op.setResponses(
                new ApiResponses().addApiResponse("200", new ApiResponse().description("ok")));
        Paths paths = new Paths();
        paths.put("/pets/", new PathItem().get(op));
        api.setPaths(paths);

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("DES004");
    }
}
