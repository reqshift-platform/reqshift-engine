package com.reqshift.rules.documentation;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

public final class RuleInfoContactProvided implements Rule {

    @Override
    public String id() {
        return "DOC002";
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
        Contact contact = info.getContact();
        boolean missing =
                contact == null || (isBlank(contact.getEmail()) && isBlank(contact.getUrl()));
        if (missing) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "info.contact is missing or has neither email nor url",
                            "#/info/contact",
                            "Add at least an email or a url so consumers know who to reach about the API."));
        }
        return List.of();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
