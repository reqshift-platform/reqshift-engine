package com.reqshift.rules.documentation;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleInfoDescriptionRequired implements Rule {

    @Override
    public String id() {
        return "DOC001";
    }

    @Override
    public Severity severity() {
        return Severity.INFO;
    }

    @Override
    public Category category() {
        return Category.DOCUMENTATION;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        var info = openApi.getInfo();
        if (info == null || info.getDescription() == null || info.getDescription().isBlank()) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "API is missing a top-level info.description",
                            "#/info/description",
                            "Add a clear, multi-line description of what the API does and who it is for"));
        }
        return List.of();
    }
}
