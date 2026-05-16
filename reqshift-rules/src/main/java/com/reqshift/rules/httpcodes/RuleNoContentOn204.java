package com.reqshift.rules.httpcodes;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;

public final class RuleNoContentOn204 implements Rule {

    @Override
    public String id() {
        return "HTTP004";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
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
                                                    ApiResponse resp204 =
                                                            op.getResponses().get("204");
                                                    if (resp204 == null
                                                            || resp204.getContent() == null) {
                                                        return;
                                                    }
                                                    if (!resp204.getContent().isEmpty()) {
                                                        violations.add(
                                                                new Violation(
                                                                        id(),
                                                                        severity(),
                                                                        "Operation "
                                                                                + method
                                                                                + " "
                                                                                + path
                                                                                + " declares a content body on its 204 No Content response",
                                                                        "#/paths/"
                                                                                + path.replace(
                                                                                        "/", "~1")
                                                                                + "/"
                                                                                + method.toString()
                                                                                        .toLowerCase()
                                                                                + "/responses/204/content",
                                                                        "Remove the 'content' block. 204 means there is no response body."));
                                                    }
                                                }));
        return violations;
    }
}
