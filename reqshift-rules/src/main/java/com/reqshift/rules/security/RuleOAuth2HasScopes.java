package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleOAuth2HasScopes implements Rule {

    @Override
    public String id() {
        return "SEC006";
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
                            if (scheme.getType() != SecurityScheme.Type.OAUTH2
                                    || scheme.getFlows() == null) {
                                return;
                            }
                            OAuthFlows flows = scheme.getFlows();
                            checkFlow(
                                    flows.getAuthorizationCode(),
                                    name,
                                    "authorizationCode",
                                    violations);
                            checkFlow(flows.getPassword(), name, "password", violations);
                            checkFlow(
                                    flows.getClientCredentials(),
                                    name,
                                    "clientCredentials",
                                    violations);
                            checkFlow(flows.getImplicit(), name, "implicit", violations);
                        });
        return violations;
    }

    private void checkFlow(
            OAuthFlow flow, String schemeName, String flowName, List<Violation> violations) {
        if (flow == null) {
            return;
        }
        if (flow.getScopes() == null || flow.getScopes().isEmpty()) {
            violations.add(
                    new Violation(
                            id(),
                            severity(),
                            "OAuth2 flow '"
                                    + flowName
                                    + "' on scheme '"
                                    + schemeName
                                    + "' declares no scopes",
                            "#/components/securitySchemes/"
                                    + schemeName
                                    + "/flows/"
                                    + flowName
                                    + "/scopes",
                            "Declare at least one scope, even an empty-named placeholder, so consumers know what permissions to request."));
        }
    }
}
