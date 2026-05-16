package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

public final class RuleMutatingOpsHaveSecurity implements Rule {

    private static final List<PathItem.HttpMethod> MUTATING =
            List.of(
                    PathItem.HttpMethod.POST,
                    PathItem.HttpMethod.PUT,
                    PathItem.HttpMethod.PATCH,
                    PathItem.HttpMethod.DELETE);

    @Override
    public String id() {
        return "SEC009";
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
        if (openApi.getPaths() == null) {
            return List.of();
        }
        boolean hasGlobal = openApi.getSecurity() != null && !openApi.getSecurity().isEmpty();
        if (hasGlobal) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        openApi.getPaths()
                .forEach(
                        (path, pathItem) -> {
                            Map<PathItem.HttpMethod, Operation> ops = pathItem.readOperationsMap();
                            for (PathItem.HttpMethod method : MUTATING) {
                                Operation op = ops.get(method);
                                if (op == null) {
                                    continue;
                                }
                                if (op.getSecurity() == null || op.getSecurity().isEmpty()) {
                                    violations.add(
                                            new Violation(
                                                    id(),
                                                    severity(),
                                                    "Mutating operation "
                                                            + method
                                                            + " "
                                                            + path
                                                            + " has no security requirement",
                                                    "#/paths/"
                                                            + path.replace("/", "~1")
                                                            + "/"
                                                            + method.toString().toLowerCase()
                                                            + "/security",
                                                    "Add a 'security' requirement on this operation (or define a global one)."));
                                }
                            }
                        });
        return violations;
    }
}
