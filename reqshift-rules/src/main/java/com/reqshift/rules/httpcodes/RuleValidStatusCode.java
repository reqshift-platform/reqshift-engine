package com.reqshift.rules.httpcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleValidStatusCode implements Rule {

    private static final Pattern WILDCARD = Pattern.compile("[1-5]XX");

    @Override
    public String id() {
        return "HTTP003";
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
                        (path, pathItem) ->
                                pathItem.readOperationsMap()
                                        .forEach(
                                                (method, op) -> {
                                                    if (op.getResponses() == null) {
                                                        return;
                                                    }
                                                    op.getResponses()
                                                            .keySet()
                                                            .forEach(
                                                                    code -> {
                                                                        if (isValid(code)) {
                                                                            return;
                                                                        }
                                                                        violations.add(
                                                                                new Violation(
                                                                                        id(),
                                                                                        severity(),
                                                                                        "Operation "
                                                                                                + method
                                                                                                + " "
                                                                                                + path
                                                                                                + " declares invalid status code '"
                                                                                                + code
                                                                                                + "'",
                                                                                        "#/paths/"
                                                                                                + path
                                                                                                        .replace(
                                                                                                                "/",
                                                                                                                "~1")
                                                                                                + "/"
                                                                                                + method.toString()
                                                                                                        .toLowerCase()
                                                                                                + "/responses/"
                                                                                                + code,
                                                                                        "Use a standard HTTP status code (100-599), a wildcard (1XX-5XX), or 'default'."));
                                                                    });
                                                }));
        return violations;
    }

    private boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        if (code.equals("default") || WILDCARD.matcher(code).matches()) {
            return true;
        }
        try {
            int n = Integer.parseInt(code);
            return n >= 100 && n <= 599;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
