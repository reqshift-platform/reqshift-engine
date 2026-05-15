package com.reqshift.core.model;

public record Violation(
        String ruleId, Severity severity, String message, String location, String suggestion) {}
