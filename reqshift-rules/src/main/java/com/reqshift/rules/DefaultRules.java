package com.reqshift.rules;

import java.util.List;

import com.reqshift.core.model.Rule;
import com.reqshift.rules.conformance.RuleInfoTitleRequired;
import com.reqshift.rules.conformance.RuleInfoVersionRequired;
import com.reqshift.rules.conformance.RuleOpenApiVersionSupported;
import com.reqshift.rules.conformance.RuleOperationHasResponses;
import com.reqshift.rules.conformance.RuleOperationHasSuccessResponse;
import com.reqshift.rules.conformance.RuleParameterHasSchema;
import com.reqshift.rules.conformance.RulePathStartsWithSlash;
import com.reqshift.rules.conformance.RulePathsNotEmpty;
import com.reqshift.rules.conformance.RuleServerUrlValid;
import com.reqshift.rules.conformance.RuleServersDefined;
import com.reqshift.rules.design.RuleOperationIdRequired;
import com.reqshift.rules.documentation.RuleInfoDescriptionRequired;
import com.reqshift.rules.httpcodes.RuleNoBodyOnGetOrDelete;
import com.reqshift.rules.httpcodes.RuleNoContentOn204;
import com.reqshift.rules.httpcodes.RuleOperationHasErrorResponse;
import com.reqshift.rules.httpcodes.RulePost201HasLocationHeader;
import com.reqshift.rules.httpcodes.RuleValidStatusCode;
import com.reqshift.rules.security.RuleApiHasGlobalOrPerOpSecurity;
import com.reqshift.rules.security.RuleApiKeyHasName;
import com.reqshift.rules.security.RuleBearerHasFormat;
import com.reqshift.rules.security.RuleMutatingOpsHaveSecurity;
import com.reqshift.rules.security.RuleNoApiKeyInQuery;
import com.reqshift.rules.security.RuleNoBasicHttpAuth;
import com.reqshift.rules.security.RuleNoOAuth2ImplicitFlow;
import com.reqshift.rules.security.RuleOAuth2FlowHasTokenUrl;
import com.reqshift.rules.security.RuleOAuth2HasScopes;
import com.reqshift.rules.security.RuleOpenIdConnectHasUrl;
import com.reqshift.rules.security.RuleSecuritySchemesDefined;
import com.reqshift.rules.security.RuleServersUseHttps;

public final class DefaultRules {

    private DefaultRules() {}

    public static List<Rule> all() {
        return List.of(
                // Conformance (CONF001-CONF010)
                new RuleOpenApiVersionSupported(),
                new RuleInfoTitleRequired(),
                new RuleInfoVersionRequired(),
                new RuleServersDefined(),
                new RuleServerUrlValid(),
                new RulePathsNotEmpty(),
                new RulePathStartsWithSlash(),
                new RuleOperationHasResponses(),
                new RuleOperationHasSuccessResponse(),
                new RuleParameterHasSchema(),
                // Security (SEC001-SEC012)
                new RuleNoBasicHttpAuth(),
                new RuleSecuritySchemesDefined(),
                new RuleApiHasGlobalOrPerOpSecurity(),
                new RuleNoOAuth2ImplicitFlow(),
                new RuleNoApiKeyInQuery(),
                new RuleOAuth2HasScopes(),
                new RuleServersUseHttps(),
                new RuleBearerHasFormat(),
                new RuleMutatingOpsHaveSecurity(),
                new RuleApiKeyHasName(),
                new RuleOAuth2FlowHasTokenUrl(),
                new RuleOpenIdConnectHasUrl(),
                // HTTP codes (HTTP001-HTTP005)
                new RuleNoBodyOnGetOrDelete(),
                new RuleOperationHasErrorResponse(),
                new RuleValidStatusCode(),
                new RuleNoContentOn204(),
                new RulePost201HasLocationHeader(),
                // Design
                new RuleOperationIdRequired(),
                // Documentation
                new RuleInfoDescriptionRequired());
    }
}
