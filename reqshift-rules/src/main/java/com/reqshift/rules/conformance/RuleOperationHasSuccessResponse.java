package com.reqshift.rules.conformance;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponses;

public final class RuleOperationHasSuccessResponse implements Rule {

    @Override
    public String id() {
        return "CONF009";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
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
                                                    ApiResponses responses = op.getResponses();
                                                    if (responses == null || responses.isEmpty()) {
                                                        return;
                                                    }
                                                    boolean hasSuccess =
                                                            responses.keySet().stream()
                                                                    .anyMatch(
                                                                            code ->
                                                                                    code.startsWith(
                                                                                                    "2")
                                                                                            || code
                                                                                                    .equals(
                                                                                                            "default"));
                                                    if (!hasSuccess) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " has no 2xx or default response",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/responses",
                                                                        "Add a response with a 2xx status code (or a 'default' response)."));
                                                    }
                                                }));
        return violations;
    }
}
