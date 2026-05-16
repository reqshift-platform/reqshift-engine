package com.reqshift.rules.httpcodes;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponses;

public final class RuleOperationHasErrorResponse implements Rule {

    @Override
    public String id() {
        return "HTTP002";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
    }

    @Override
    public Category category() {
        return Category.HTTP_CODES;
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
                                                    boolean hasError =
                                                            responses.keySet().stream()
                                                                    .anyMatch(
                                                                            code ->
                                                                                    code.startsWith(
                                                                                                    "4")
                                                                                            || code
                                                                                                    .startsWith(
                                                                                                            "5")
                                                                                            || code
                                                                                                    .equals(
                                                                                                            "default"));
                                                    if (!hasError) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " documents no 4xx or 5xx response",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/responses",
                                                                        "Document at least one error response (or a 'default')."));
                                                    }
                                                }));
        return violations;
    }
}
