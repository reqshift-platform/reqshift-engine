package com.reqshift.core.config;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.reqshift.core.model.Severity;

public record RuleConfig(Set<String> disabled, Map<String, Severity> severityOverrides) {

    public RuleConfig {
        disabled = Set.copyOf(Objects.requireNonNullElse(disabled, Set.of()));
        severityOverrides = Map.copyOf(Objects.requireNonNullElse(severityOverrides, Map.of()));
    }

    public static RuleConfig empty() {
        return new RuleConfig(Set.of(), Map.of());
    }

    public boolean isDisabled(String ruleId) {
        return disabled.contains(ruleId);
    }

    public Severity severityFor(String ruleId, Severity fallback) {
        return severityOverrides.getOrDefault(ruleId, fallback);
    }
}
