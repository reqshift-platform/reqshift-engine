package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleNoVerbInPath implements Rule {

    private static final Set<String> COMMON_VERBS =
            Set.of(
                    "get",
                    "fetch",
                    "list",
                    "find",
                    "search",
                    "retrieve",
                    "load",
                    "create",
                    "add",
                    "insert",
                    "post",
                    "save",
                    "update",
                    "modify",
                    "edit",
                    "patch",
                    "put",
                    "set",
                    "delete",
                    "remove",
                    "destroy",
                    "send",
                    "execute",
                    "run",
                    "process");

    @Override
    public String id() {
        return "DES005";
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
                if (segment.isEmpty() || (segment.startsWith("{") && segment.endsWith("}"))) {
                    continue;
                }
                String verb = leadingVerb(segment);
                if (verb != null) {
                    violations.add(
                            new Violation(
                                    id(),
                                    severity(),
                                    "Path '"
                                            + path
                                            + "' contains the verb '"
                                            + verb
                                            + "' in segment '"
                                            + segment
                                            + "'",
                                    "#/paths/" + path.replace("/", "~1"),
                                    "Use nouns for resources and let HTTP methods convey the action."));
                    break;
                }
            }
        }
        return violations;
    }

    private String leadingVerb(String segment) {
        String lower = segment.toLowerCase();
        for (String verb : COMMON_VERBS) {
            if (!lower.startsWith(verb)) {
                continue;
            }
            if (segment.length() == verb.length()) {
                return verb;
            }
            char next = segment.charAt(verb.length());
            if (next == '-' || next == '_' || Character.isUpperCase(next)) {
                return verb;
            }
        }
        return null;
    }
}
