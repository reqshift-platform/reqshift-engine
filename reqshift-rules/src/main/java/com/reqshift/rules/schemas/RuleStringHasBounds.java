package com.reqshift.rules.schemas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public final class RuleStringHasBounds implements Rule {

    private static final Set<String> BOUNDED_FORMATS =
            Set.of(
                    "date",
                    "date-time",
                    "time",
                    "email",
                    "uuid",
                    "uri",
                    "url",
                    "hostname",
                    "ipv4",
                    "ipv6",
                    "byte");

    @Override
    public String id() {
        return "SCHEMAS004";
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
                        (schemaName, schema) -> {
                            if (schema.getProperties() == null) {
                                return;
                            }
                            schema.getProperties()
                                    .forEach(
                                            (propName, propSchema) -> {
                                                Schema<?> p = (Schema<?>) propSchema;
                                                if (!"string".equals(p.getType())) {
                                                    return;
                                                }
                                                if (isBounded(p)) {
                                                    return;
                                                }
                                                violations.add(
                                                        new Violation(
                                                                id(),
                                                                severity(),
                                                                "Property '"
                                                                        + propName
                                                                        + "' in schema '"
                                                                        + schemaName
                                                                        + "' is an unbounded string (no maxLength, format, pattern, or enum)",
                                                                "#/components/schemas/"
                                                                        + schemaName
                                                                        + "/properties/"
                                                                        + propName,
                                                                "Add 'maxLength', 'format', 'pattern', or 'enum' to bound the value and prevent oversized payloads."));
                                            });
                        });
        return violations;
    }

    private boolean isBounded(Schema<?> p) {
        if (p.getMaxLength() != null) {
            return true;
        }
        if (p.getEnum() != null && !p.getEnum().isEmpty()) {
            return true;
        }
        if (p.getPattern() != null && !p.getPattern().isBlank()) {
            return true;
        }
        String format = p.getFormat();
        return format != null && BOUNDED_FORMATS.contains(format.toLowerCase());
    }
}
