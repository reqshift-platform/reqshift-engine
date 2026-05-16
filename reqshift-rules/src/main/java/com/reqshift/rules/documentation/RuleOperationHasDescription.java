package com.reqshift.rules.documentation;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleOperationHasDescription implements Rule {

    @Override
    public String id() {
        return "DOC004";
    }

    @Override
    public Severity severity() {
        return Severity.INFO;
    }

    @Override
    public Category category() {
        return Category.DOCUMENTATION;
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
                                                    if (op.getDescription() == null
                                                            || op.getDescription().isBlank()) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " has no description",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/description",
                                                                        "Add a 'description' field that goes beyond the short 'summary' (Markdown allowed)."));
                                                    }
                                                }));
        return violations;
    }
}
