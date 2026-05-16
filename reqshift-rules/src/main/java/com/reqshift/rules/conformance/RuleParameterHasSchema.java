package com.reqshift.rules.conformance;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

public final class RuleParameterHasSchema implements Rule {

    @Override
    public String id() {
        return "CONF010";
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
            if (p.getSchema() == null && p.getContent() == null) {
                String base = "#/paths/" + path.replace("/", "~1");
                String location =
                        method == null
                                ? base + "/parameters/" + p.getName()
                                : base + "/" + method + "/parameters/" + p.getName();
                violations.add(
                        new Violation(
                                id(),
                                severity(),
                                "Parameter '"
                                        + p.getName()
                                        + "' has neither a schema nor a content object",
                                location,
                                "Declare the parameter type using a 'schema' object (preferred) or a 'content' map."));
            }
        }
    }
}
