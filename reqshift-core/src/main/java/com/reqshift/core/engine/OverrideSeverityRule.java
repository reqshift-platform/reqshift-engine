package com.reqshift.core.engine;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class OverrideSeverityRule implements Rule {

    private final Rule delegate;
    private final Severity override;

    public OverrideSeverityRule(Rule delegate, Severity override) {
        this.delegate = delegate;
        this.override = override;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public Severity severity() {
        return override;
    }

    @Override
    public Category category() {
        return delegate.category();
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        return delegate.check(openApi).stream()
                .map(
                        v ->
                                new Violation(
                                        v.ruleId(),
                                        override,
                                        v.message(),
                                        v.location(),
                                        v.suggestion()))
                .toList();
    }
}
