package com.reqshift.rules.documentation;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;

public final class RuleTagHasDescription implements Rule {

    @Override
    public String id() {
        return "DOC008";
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
        if (openApi.getTags() == null || openApi.getTags().isEmpty()) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        int idx = 0;
        for (Tag tag : openApi.getTags()) {
            if (tag.getDescription() == null || tag.getDescription().isBlank()) {
                violations.add(
                        new Violation(
                                id(),
                                severity(),
                                "Tag '" + tag.getName() + "' has no description",
                                "#/tags/" + idx + "/description",
                                "Describe what the tag groups so generated docs make sense."));
            }
            idx++;
        }
        return violations;
    }
}
