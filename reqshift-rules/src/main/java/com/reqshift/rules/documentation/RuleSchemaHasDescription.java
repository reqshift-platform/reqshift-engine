package com.reqshift.rules.documentation;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleSchemaHasDescription implements Rule {

    @Override
    public String id() {
        return "DOC006";
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
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        openApi.getComponents()
                .getSchemas()
                .forEach(
                        (name, schema) -> {
                            String desc = schema.getDescription();
                            if (desc == null || desc.isBlank()) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "Schema '" + name + "' has no description",
                                                "#/components/schemas/" + name + "/description",
                                                "Add a description explaining what this schema represents."));
                            }
                        });
        return violations;
    }
}
