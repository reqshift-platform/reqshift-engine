package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleBearerHasFormat implements Rule {

    @Override
    public String id() {
        return "SEC008";
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
        if (openApi.getComponents() == null
                || openApi.getComponents().getSecuritySchemes() == null) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        openApi.getComponents()
                .getSecuritySchemes()
                .forEach(
                        (name, scheme) -> {
                            boolean isHttpBearer =
                                    scheme.getType() == SecurityScheme.Type.HTTP
                                            && "bearer".equalsIgnoreCase(scheme.getScheme());
                            if (isHttpBearer
                                    && (scheme.getBearerFormat() == null
                                            || scheme.getBearerFormat().isBlank())) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "Bearer security scheme '"
                                                        + name
                                                        + "' declares no bearerFormat",
                                                "#/components/securitySchemes/"
                                                        + name
                                                        + "/bearerFormat",
                                                "Declare the format (for example: bearerFormat: JWT) so consumers know what token to send."));
                            }
                        });
        return violations;
    }
}
