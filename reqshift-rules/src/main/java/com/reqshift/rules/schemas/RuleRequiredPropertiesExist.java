package com.reqshift.rules.schemas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleRequiredPropertiesExist implements Rule {

    @Override
    public String id() {
        return "SCHEMAS002";
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
                            List<String> required = schema.getRequired();
                            if (required == null || required.isEmpty()) {
                                return;
                            }
                            Set<String> declared =
                                    schema.getProperties() == null
                                            ? Set.of()
                                            : schema.getProperties().keySet();
                            for (String req : required) {
                                if (!declared.contains(req)) {
                                    violations.add(
                                            new Violation(
                                                    id(),
                                                    severity(),
                                                    "Schema '"
                                                            + name
                                                            + "' marks '"
                                                            + req
                                                            + "' as required but no such property is declared",
                                                    "#/components/schemas/" + name + "/required",
                                                    "Either remove '"
                                                            + req
                                                            + "' from required, or add the property to 'properties'."));
                                }
                            }
                        });
        return violations;
    }
}
