package com.reqshift.rules.security;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleApiHasGlobalOrPerOpSecurity implements Rule {

    @Override
    public String id() {
        return "SEC003";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
    }

    @Override
    public Category category() {
        return Category.SECURITY;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        if (openApi.getPaths() == null || openApi.getPaths().isEmpty()) {
            return List.of();
        }
        boolean hasGlobal = openApi.getSecurity() != null && !openApi.getSecurity().isEmpty();
        if (hasGlobal) {
            return List.of();
        }
        boolean anyOpHasSecurity =
                openApi.getPaths().values().stream()
                        .flatMap(p -> p.readOperations().stream())
                        .anyMatch(op -> op.getSecurity() != null && !op.getSecurity().isEmpty());
        if (!anyOpHasSecurity) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "No security requirement is applied anywhere in the API. Every operation is publicly accessible.",
                            "#/security",
                            "Add a global 'security' block or apply 'security' on each operation."));
        }
        return List.of();
    }
}
