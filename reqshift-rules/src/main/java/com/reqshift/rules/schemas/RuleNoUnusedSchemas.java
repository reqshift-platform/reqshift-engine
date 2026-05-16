package com.reqshift.rules.schemas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleNoUnusedSchemas implements Rule {

    private static final Pattern REF =
            Pattern.compile("\"\\$ref\"\\s*:\\s*\"#/components/schemas/([^\"]+)\"");

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String id() {
        return "SCHEMAS005";
    }

    @Override
    public Severity severity() {
        return Severity.INFO;
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
        Set<String> declared = openApi.getComponents().getSchemas().keySet();
        Set<String> referenced = collectReferenced(openApi);
        List<Violation> violations = new ArrayList<>();
        for (String name : declared) {
            if (!referenced.contains(name)) {
                violations.add(
                        new Violation(
                                id(),
                                severity(),
                                "Schema '"
                                        + name
                                        + "' is defined in components/schemas but never referenced",
                                "#/components/schemas/" + name,
                                "Remove the unused schema or reference it via $ref where it applies."));
            }
        }
        return violations;
    }

    private Set<String> collectReferenced(OpenAPI api) {
        Set<String> refs = new HashSet<>();
        try {
            String json = mapper.writeValueAsString(api);
            Matcher m = REF.matcher(json);
            while (m.find()) {
                refs.add(m.group(1));
            }
        } catch (Exception ignored) {
            // If serialization fails we cannot detect refs, so report none unused.
            refs.addAll(api.getComponents().getSchemas().keySet());
        }
        return refs;
    }
}
