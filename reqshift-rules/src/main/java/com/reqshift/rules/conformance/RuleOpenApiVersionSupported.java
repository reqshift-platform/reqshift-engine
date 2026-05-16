package com.reqshift.rules.conformance;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleOpenApiVersionSupported implements Rule {

    @Override
    public String id() {
        return "CONF001";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Category category() {
        return Category.CONFORMANCE;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        String version = openApi.getOpenapi();
        if (version == null || !(version.startsWith("3.0") || version.startsWith("3.1"))) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "Unsupported OpenAPI version: "
                                    + (version == null ? "(missing)" : version)
                                    + ". ReqShift supports 3.0.x and 3.1.x.",
                            "#/openapi",
                            "Set 'openapi: 3.0.3' or 'openapi: 3.1.0' at the root of the document."));
        }
        return List.of();
    }
}
