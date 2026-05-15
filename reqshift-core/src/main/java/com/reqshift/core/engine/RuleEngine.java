package com.reqshift.core.engine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reqshift.core.model.Rule;
import com.reqshift.core.model.RuleResult;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<RuleResult> run(OpenAPI openApi) {
        log.debug("Running {} rules against OpenAPI document", rules.size());
        return rules.stream()
                .map(
                        rule -> {
                            List<com.reqshift.core.model.Violation> violations =
                                    rule.check(openApi);
                            log.debug(
                                    "Rule {} produced {} violations", rule.id(), violations.size());
                            return new RuleResult(rule, violations);
                        })
                .toList();
    }
}
