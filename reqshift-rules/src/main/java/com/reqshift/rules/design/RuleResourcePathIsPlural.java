package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleResourcePathIsPlural implements Rule {

    private static final Set<String> SINGULAR_OK =
            Set.of(
                    "health",
                    "status",
                    "info",
                    "version",
                    "metadata",
                    "me",
                    "self",
                    "ping",
                    "ready",
                    "live",
                    "config",
                    "auth",
                    "login",
                    "logout",
                    "register",
                    "search",
                    "stats");

    @Override
    public String id() {
        return "DES003";
    }

    @Override
    public Severity severity() {
        return Severity.INFO;
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
            String last = lastStaticSegment(path);
            if (last == null) {
                continue;
            }
            String lower = last.toLowerCase();
            if (SINGULAR_OK.contains(lower) || lower.endsWith("s")) {
                continue;
            }
            violations.add(
                    new Violation(
                            id(),
                            severity(),
                            "Resource path '"
                                    + path
                                    + "' uses singular '"
                                    + last
                                    + "' (collections should be plural)",
                            "#/paths/" + path.replace("/", "~1"),
                            "Use a plural noun for collection resources, e.g. /users instead of /user."));
        }
        return violations;
    }

    private String lastStaticSegment(String path) {
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            String s = parts[i];
            if (!s.isEmpty() && !(s.startsWith("{") && s.endsWith("}"))) {
                return s;
            }
        }
        return null;
    }
}
