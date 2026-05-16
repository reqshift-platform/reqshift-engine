package com.reqshift.rules.conformance;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RulePathsNotEmpty implements Rule {

    @Override
    public String id() {
        return "CONF006";
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
        if (openApi.getPaths() == null || openApi.getPaths().isEmpty()) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "API exposes no paths. An OpenAPI document without operations is rarely useful.",
                            "#/paths",
                            "Document at least one operation (path + method)."));
        }
        return List.of();
    }
}
