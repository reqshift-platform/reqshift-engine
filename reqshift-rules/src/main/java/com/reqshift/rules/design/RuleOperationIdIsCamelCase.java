package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleOperationIdIsCamelCase implements Rule {

    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z][a-zA-Z0-9]*$");

    @Override
    public String id() {
        return "DES010";
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
                                                (method, op) -> {
                                                    String opId = op.getOperationId();
                                                    if (opId == null || opId.isBlank()) {
                                                        return;
                                                    }
                                                    if (!CAMEL_CASE.matcher(opId).matches()) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "operationId '"
                                                                                + opId
                                                                                + "' on "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " is not camelCase",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/operationId",
                                                                        "Use camelCase, e.g. listPets, getPetById, createOrder."));
                                                    }
                                                }));
        return violations;
    }
}
