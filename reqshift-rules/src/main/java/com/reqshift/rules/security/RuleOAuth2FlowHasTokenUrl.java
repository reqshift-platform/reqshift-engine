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

public final class RuleOAuth2FlowHasTokenUrl implements Rule {

    @Override
    public String id() {
        return "SEC011";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
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
                        (schemeName, scheme) -> {
                            if (scheme.getType() != SecurityScheme.Type.OAUTH2
                                    || scheme.getFlows() == null) {
                                return;
                            }
                            OAuthFlows flows = scheme.getFlows();
                            checkTokenUrl(
                                    flows.getAuthorizationCode(),
                                    schemeName,
                                    "authorizationCode",
                                    violations);
                            checkTokenUrl(flows.getPassword(), schemeName, "password", violations);
                            checkTokenUrl(
                                    flows.getClientCredentials(),
                                    schemeName,
                                    "clientCredentials",
                                    violations);
                        });
        return violations;
    }

    private void checkTokenUrl(
            OAuthFlow flow, String schemeName, String flowName, List<Violation> violations) {
        if (flow == null) {
            return;
        }
        if (flow.getTokenUrl() == null || flow.getTokenUrl().isBlank()) {
            violations.add(
                    new Violation(
                            id(),
                            severity(),
                            "OAuth2 "
                                    + flowName
                                    + " flow on scheme '"
                                    + schemeName
                                    + "' has no tokenUrl",
                            "#/components/securitySchemes/"
                                    + schemeName
                                    + "/flows/"
                                    + flowName
                                    + "/tokenUrl",
                            "Declare the tokenUrl used to obtain access tokens."));
        }
    }
}
