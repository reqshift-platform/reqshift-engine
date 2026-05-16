package com.reqshift.rules.schemas;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleObjectSchemaHasProperties implements Rule {

    @Override
    public String id() {
        return "SCHEMAS001";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
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
                            if (!"object".equals(schema.getType())) {
                                return;
                            }
                            boolean noProps =
                                    schema.getProperties() == null
                                            || schema.getProperties().isEmpty();
                            boolean noAddProps = schema.getAdditionalProperties() == null;
                            boolean noComposition =
                                    schema.getAllOf() == null
                                            && schema.getOneOf() == null
                                            && schema.getAnyOf() == null;
                            if (noProps && noAddProps && noComposition) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "Object schema '"
                                                        + name
                                                        + "' declares neither properties, additionalProperties, nor a composition (allOf/oneOf/anyOf)",
                                                "#/components/schemas/" + name + "/properties",
                                                "List 'properties', set 'additionalProperties', or compose via allOf/oneOf/anyOf to define the shape."));
                            }
                        });
        return violations;
    }
}
