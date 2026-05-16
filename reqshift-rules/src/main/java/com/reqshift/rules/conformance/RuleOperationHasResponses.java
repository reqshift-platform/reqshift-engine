package com.reqshift.rules.conformance;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleOperationHasResponses implements Rule {

    @Override
    public String id() {
        return "CONF008";
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
        if (openApi.getPaths() == null) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        openApi.getPaths()
                .forEach(
                        (path, pathItem) ->
                                pathItem.readOperationsMap()
                                        .forEach(
                                                (method, op) -> {
                                                    if (op.getResponses() == null
                                                            || op.getResponses().isEmpty()) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " has no responses defined",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/responses",
                                                                        "Declare at least one response, including a 2xx success code."));
                                                    }
                                                }));
        return violations;
    }
}
