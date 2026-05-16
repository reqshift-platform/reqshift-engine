package com.reqshift.rules.httpcodes;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

public final class RuleNoBodyOnGetOrDelete implements Rule {

    @Override
    public String id() {
        return "HTTP001";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Category category() {
        return Category.HTTP_CODES;
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
                            checkOp(pathItem.getGet(), "GET", path, violations);
                            checkOp(pathItem.getDelete(), "DELETE", path, violations);
                        });
        return violations;
    }

    private void checkOp(Operation op, String method, String path, List<Violation> violations) {
        if (op == null || op.getRequestBody() == null) {
            return;
        }
        String advice =
                method.equals("GET")
                        ? "Remove the requestBody and use query or path parameters instead."
                        : "Remove the requestBody. DELETE operations should be idempotent and bodyless.";
        violations.add(
                new Violation(
                        id(),
                        severity(),
                        method
                                + " "
                                + path
                                + " declares a requestBody. "
                                + method
                                + " operations should not have a body per RFC 9110.",
                        "#/paths/"
                                + path.replace("/", "~1")
                                + "/"
                                + method.toLowerCase()
                                + "/requestBody",
                        advice));
    }
}
