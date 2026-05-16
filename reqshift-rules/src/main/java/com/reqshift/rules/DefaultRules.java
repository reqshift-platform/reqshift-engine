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
import com.reqshift.rules.design.RuleNoTrailingSlash;
import com.reqshift.rules.design.RuleNoVerbInPath;
import com.reqshift.rules.design.RuleOperationHasSummary;
import com.reqshift.rules.design.RuleOperationHasTags;
import com.reqshift.rules.design.RuleOperationIdIsCamelCase;
import com.reqshift.rules.design.RuleOperationIdRequired;
import com.reqshift.rules.design.RuleOperationIdUnique;
import com.reqshift.rules.design.RulePathParameterDocumented;
import com.reqshift.rules.design.RulePathSegmentKebabCase;
import com.reqshift.rules.design.RuleResourcePathIsPlural;
import com.reqshift.rules.documentation.RuleInfoContactProvided;
import com.reqshift.rules.documentation.RuleInfoDescriptionRequired;
import com.reqshift.rules.documentation.RuleInfoLicenseProvided;
import com.reqshift.rules.documentation.RuleOperationHasDescription;
import com.reqshift.rules.documentation.RuleParameterHasDescription;
import com.reqshift.rules.documentation.RuleSchemaHasDescription;
import com.reqshift.rules.documentation.RuleSchemaHasExample;
import com.reqshift.rules.documentation.RuleTagHasDescription;
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
                // Design (DES001-DES010)
                new RuleOperationIdRequired(),
                new RulePathSegmentKebabCase(),
                new RuleResourcePathIsPlural(),
                new RuleNoTrailingSlash(),
                new RuleNoVerbInPath(),
                new RuleOperationIdUnique(),
                new RuleOperationHasSummary(),
                new RuleOperationHasTags(),
                new RulePathParameterDocumented(),
                new RuleOperationIdIsCamelCase(),
                // Documentation (DOC001-DOC008)
                new RuleInfoDescriptionRequired(),
                new RuleInfoContactProvided(),
                new RuleInfoLicenseProvided(),
                new RuleOperationHasDescription(),
                new RuleParameterHasDescription(),
                new RuleSchemaHasDescription(),
                new RuleSchemaHasExample(),
                new RuleTagHasDescription());
    }
}
