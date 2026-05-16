package com.reqshift.core.engine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reqshift.core.config.RuleConfig;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Severity;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static RuleEngine fromRules(List<Rule> rules, RuleConfig config) {
        if (config == null) {
            return new RuleEngine(rules);
        }
        List<Rule> filtered = new ArrayList<>(rules.size());
        for (Rule rule : rules) {
            if (config.isDisabled(rule.id())) {
                log.debug("Rule {} disabled by configuration", rule.id());
                continue;
            }
            Severity override = config.severityFor(rule.id(), rule.severity());
            if (override != rule.severity()) {
                log.debug(
                        "Rule {} severity overridden: {} -> {}",
                        rule.id(),
                        rule.severity(),
                        override);
                filtered.add(new OverrideSeverityRule(rule, override));
            } else {
                filtered.add(rule);
            }
        }
        return new RuleEngine(filtered);
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
