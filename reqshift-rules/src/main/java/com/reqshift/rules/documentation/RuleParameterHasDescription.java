package com.reqshift.rules.documentation;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

public final class RuleParameterHasDescription implements Rule {

    @Override
    public String id() {
        return "DOC005";
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
                        (path, pathItem) -> {
                            checkParams(pathItem.getParameters(), path, null, violations);
                            pathItem.readOperationsMap()
                                    .forEach(
                                            (method, op) ->
                                                    checkParams(
                                                            op.getParameters(),
                                                            path,
                                                            method.toString().toLowerCase(),
                                                            violations));
                        });
        return violations;
    }

    private void checkParams(
            List<Parameter> params, String path, String method, List<Violation> violations) {
        if (params == null) {
            return;
        }
        for (Parameter p : params) {
            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                continue;
            }
            String base = "#/paths/" + path.replace("/", "~1");
            String location =
                    method == null
                            ? base + "/parameters/" + p.getName()
                            : base + "/" + method + "/parameters/" + p.getName();
            violations.add(
                    new Violation(
                            id(),
                            severity(),
                            "Parameter '" + p.getName() + "' on " + path + " has no description",
                            location,
                            "Describe what the parameter represents and any constraints."));
        }
    }
}
