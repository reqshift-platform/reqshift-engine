package com.reqshift.rules.schemas;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public final class RuleArrayHasItems implements Rule {

    @Override
    public String id() {
        return "SCHEMAS003";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Category category() {
        return Category.SCHEMAS;
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
                            checkArray(schema, name, "#/components/schemas/" + name, violations);
                            if (schema.getProperties() != null) {
                                schema.getProperties()
                                        .forEach(
                                                (propName, propSchema) ->
                                                        checkArray(
                                                                (Schema<?>) propSchema,
                                                                name + "." + propName,
                                                                "#/components/schemas/"
                                                                        + name
                                                                        + "/properties/"
                                                                        + propName,
                                                                violations));
                            }
                        });
        return violations;
    }

    private void checkArray(
            Schema<?> schema, String contextName, String location, List<Violation> violations) {
        if (schema == null) {
            return;
        }
        if ("array".equals(schema.getType()) && schema.getItems() == null) {
            violations.add(
                    new Violation(
                            id(),
                            severity(),
                            "Array schema '" + contextName + "' has no 'items' declared",
                            location + "/items",
                            "Declare 'items' describing the element schema, e.g. items: {type: string}."));
        }
    }
}
