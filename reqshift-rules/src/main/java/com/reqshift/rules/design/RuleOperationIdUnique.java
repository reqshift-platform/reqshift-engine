package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleOperationIdUnique implements Rule {

    @Override
    public String id() {
        return "DES006";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
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
        Map<String, List<String>> seen = new LinkedHashMap<>();
        openApi.getPaths()
                .forEach(
                        (path, pathItem) ->
                                pathItem.readOperationsMap()
                                        .forEach(
                                                (method, op) -> {
                                                    String opId = op.getOperationId();
                                                    if (opId == null || opId.isBlank()) {
                                                        return;
                                                    }
                                                    seen.computeIfAbsent(
                                                                    opId, k -> new ArrayList<>())
                                                            .add(method + " " + path);
                                                }));

        List<Violation> violations = new ArrayList<>();
        seen.forEach(
                (opId, locations) -> {
                    if (locations.size() > 1) {
                        violations.add(
                                new Violation(
                                        id(),
                                        severity(),
                                        "operationId '"
                                                + opId
                                                + "' is used by multiple operations: "
                                                + String.join(", ", locations),
                                        "#/paths",
                                        "Operation IDs must be unique across the API."));
                    }
                });
        return violations;
    }
}
