package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class RuleParameterHasSchemaTest {

    private final RuleParameterHasSchema rule = new RuleParameterHasSchema();

    @Test
    void passesWhenParameterHasSchema() {
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
                          schema: {type: string}
                      responses:
                        '200': {description: ok}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsParameterWithoutSchemaOrContent() {
        // swagger-parser may complain about a schema-less parameter, so we build the model.
        OpenAPI api = new OpenAPI();
        Paths paths = new Paths();
        Operation op = new Operation();
        Parameter param = new Parameter().name("petId").in("path").required(true);
        op.addParametersItem(param);
        ApiResponses responses =
                new ApiResponses().addApiResponse("200", new ApiResponse().description("ok"));
        op.setResponses(responses);
        paths.put("/pets/{petId}", new PathItem().get(op));
        api.setPaths(paths);

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("CONF010");
        assertThat(violations.get(0).message()).contains("petId");
    }
}
