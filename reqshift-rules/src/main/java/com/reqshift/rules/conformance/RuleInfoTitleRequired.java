package com.reqshift.rules.conformance;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

public final class RuleInfoTitleRequired implements Rule {

    @Override
    public String id() {
        return "CONF002";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
    }

    @Override
    public Category category() {
        return Category.CONFORMANCE;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        Info info = openApi.getInfo();
        if (info == null || info.getTitle() == null || info.getTitle().isBlank()) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "info.title is missing or empty",
                            "#/info/title",
                            "Set a clear, human-readable title for the API."));
        }
        return List.of();
    }
}
