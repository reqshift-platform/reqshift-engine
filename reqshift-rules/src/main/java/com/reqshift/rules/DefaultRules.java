package com.reqshift.rules;

import java.util.List;

import com.reqshift.core.model.Rule;
import com.reqshift.rules.design.RuleOperationIdRequired;
import com.reqshift.rules.documentation.RuleInfoDescriptionRequired;
import com.reqshift.rules.security.RuleNoBasicHttpAuth;

public final class DefaultRules {

    private DefaultRules() {}

    public static List<Rule> all() {
        return List.of(
                new RuleOperationIdRequired(),
                new RuleNoBasicHttpAuth(),
                new RuleInfoDescriptionRequired());
    }
}
