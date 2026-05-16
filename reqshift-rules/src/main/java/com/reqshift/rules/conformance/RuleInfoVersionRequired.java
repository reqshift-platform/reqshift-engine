package com.reqshift.rules.conformance;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

public final class RuleInfoVersionRequired implements Rule {

    @Override
    public String id() {
        return "CONF003";
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
        if (info == null || info.getVersion() == null || info.getVersion().isBlank()) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "info.version is missing or empty",
                            "#/info/version",
                            "Set an API version such as '1.0.0' or '2024-05-15'."));
        }
        return List.of();
    }
}
