package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleOperationIdRequired implements Rule {

    @Override
    public String id() {
        return "DES001";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
    }

    @Override
    public Category category() {
        return Category.DESIGN;
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
                                                (method, operation) -> {
                                                    if (operation.getOperationId() == null
                                                            || operation
                                                                    .getOperationId()
                                                                    .isBlank()) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation %s %s is missing an operationId"
                                                                                .formatted(
                                                                                        method,
                                                                                        path),
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase(),
                                                                        "Add a unique operationId (camelCase verb + resource)"));
                                                    }
                                                }));
        return violations;
    }
}
