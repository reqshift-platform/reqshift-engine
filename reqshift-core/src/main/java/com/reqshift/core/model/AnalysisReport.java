package com.reqshift.core.model;

import java.util.List;

public record AnalysisReport(List<RuleResult> results, Score score, String openApiSource) {}
