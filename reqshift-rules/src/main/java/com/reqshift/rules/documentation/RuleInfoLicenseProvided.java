package com.reqshift.rules.documentation;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

public final class RuleInfoLicenseProvided implements Rule {

    @Override
    public String id() {
        return "DOC003";
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
        Info info = openApi.getInfo();
        if (info == null) {
            return List.of();
        }
        License license = info.getLicense();
        if (license == null || license.getName() == null || license.getName().isBlank()) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "info.license is missing or has no name",
                            "#/info/license",
                            "Declare a license, e.g. {name: 'Apache 2.0', url: 'https://www.apache.org/licenses/LICENSE-2.0'}."));
        }
        return List.of();
    }
}
