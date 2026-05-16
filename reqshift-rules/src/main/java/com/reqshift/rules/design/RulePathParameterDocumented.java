package com.reqshift.rules.design;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

public final class RulePathParameterDocumented implements Rule {

    private static final Pattern TEMPLATE = Pattern.compile("\\{([^{}]+)\\}");

    @Override
    public String id() {
        return "DES009";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
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
                        (path, pathItem) -> {
                            Set<String> templates = extractTemplateNames(path);
                            if (templates.isEmpty()) {
                                return;
                            }
                            Set<String> pathLevel = collectPathParamNames(pathItem.getParameters());
                            pathItem.readOperationsMap()
                                    .forEach(
                                            (method, op) -> {
                                                Set<String> available = new HashSet<>(pathLevel);
                                                available.addAll(
                                                        collectPathParamNames(op.getParameters()));
                                                for (String name : templates) {
                                                    if (!available.contains(name)) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " references template '"
                                                                                + name
                                                                                + "' but has no matching path parameter",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/parameters",
                                                                        "Declare a parameter with name: "
                                                                                + name
                                                                                + " and in: path."));
                                                    }
                                                }
                                            });
                        });
        return violations;
    }

    private Set<String> extractTemplateNames(String path) {
        Set<String> names = new LinkedHashSet<>();
        Matcher m = TEMPLATE.matcher(path);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private Set<String> collectPathParamNames(List<Parameter> params) {
        Set<String> names = new HashSet<>();
        if (params == null) {
            return names;
        }
        for (Parameter p : params) {
            if ("path".equals(p.getIn()) && p.getName() != null) {
                names.add(p.getName());
            }
        }
        return names;
    }
}
