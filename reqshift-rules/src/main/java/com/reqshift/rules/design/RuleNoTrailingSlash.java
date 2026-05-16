package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleNoTrailingSlash implements Rule {

    @Override
    public String id() {
        return "DES004";
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
        for (String path : openApi.getPaths().keySet()) {
            if (path.length() > 1 && path.endsWith("/")) {
                violations.add(
                        new Violation(
                                id(),
                                severity(),
                                "Path '" + path + "' ends with a trailing slash",
                                "#/paths/" + path.replace("/", "~1"),
                                "Remove the trailing slash to keep paths consistent."));
            }
        }
        return violations;
    }
}
