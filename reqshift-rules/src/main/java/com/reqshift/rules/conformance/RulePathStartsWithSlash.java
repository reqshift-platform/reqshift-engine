package com.reqshift.rules.conformance;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RulePathStartsWithSlash implements Rule {

    @Override
    public String id() {
        return "CONF007";
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
        for (String path : openApi.getPaths().keySet()) {
            if (!path.startsWith("/")) {
                violations.add(
                        new Violation(
                                id(),
                                severity(),
                                "Path '" + path + "' does not start with a slash",
                                "#/paths/" + path.replace("/", "~1"),
                                "Prefix the path with /, for example /" + path));
            }
        }
        return violations;
    }
}
