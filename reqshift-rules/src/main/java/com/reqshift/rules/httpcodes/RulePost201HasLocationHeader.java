package com.reqshift.rules.httpcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponse;

public final class RulePost201HasLocationHeader implements Rule {

    @Override
    public String id() {
        return "HTTP005";
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
                        (path, pathItem) -> {
                            Operation post = pathItem.getPost();
                            if (post == null || post.getResponses() == null) {
                                return;
                            }
                            ApiResponse resp201 = post.getResponses().get("201");
                            if (resp201 == null) {
                                return;
                            }
                            Map<String, Header> headers = resp201.getHeaders();
                            boolean hasLocation =
                                    headers != null
                                            && headers.keySet().stream()
                                                    .anyMatch(h -> h.equalsIgnoreCase("Location"));
                            if (!hasLocation) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "POST "
                                                        + path
                                                        + " returns 201 Created but declares no 'Location' response header",
                                                "#/paths/"
                                                        + path.replace("/", "~1")
                                                        + "/post/responses/201/headers",
                                                "Add a 'Location' response header pointing to the newly created resource."));
                            }
                        });
        return violations;
    }
}
