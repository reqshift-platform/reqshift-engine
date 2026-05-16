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
import com.reqshift.rules.security.RuleNoBasicHttpAuth;

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
                // Design
                new RuleOperationIdRequired(),
                // Security
                new RuleNoBasicHttpAuth(),
                // Documentation
                new RuleInfoDescriptionRequired());
    }
}
