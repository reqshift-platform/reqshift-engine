package com.reqshift.core.model;

import java.util.List;

public record RuleResult(Rule rule, List<Violation> violations) {}
