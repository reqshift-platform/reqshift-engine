package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RulePathSegmentKebabCase implements Rule {

    private static final Pattern KEBAB = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    @Override
    public String id() {
        return "DES002";
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
            for (String segment : path.split("/")) {
                if (segment.isEmpty() || isTemplate(segment)) {
                    continue;
                }
                if (!KEBAB.matcher(segment).matches()) {
                    violations.add(
                            new Violation(
                                    id(),
                                    severity(),
                                    "Path segment '"
                                            + segment
                                            + "' in '"
                                            + path
                                            + "' is not kebab-case",
                                    "#/paths/" + path.replace("/", "~1"),
                                    "Use lowercase letters, digits and hyphens. Example: /user-profiles."));
                    break;
                }
            }
        }
        return violations;
    }

    private boolean isTemplate(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }
}
