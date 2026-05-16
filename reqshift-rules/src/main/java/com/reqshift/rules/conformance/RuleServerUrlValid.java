package com.reqshift.rules.conformance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

public final class RuleServerUrlValid implements Rule {

    @Override
    public String id() {
        return "CONF005";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Category category() {
        return Category.CONFORMANCE;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        if (openApi.getServers() == null) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        int idx = 0;
        for (Server server : openApi.getServers()) {
            String location = "#/servers/" + idx;
            String url = server.getUrl();
            if (url == null || url.isBlank()) {
                violations.add(
                        new Violation(
                                id(),
                                severity(),
                                "Server URL is missing",
                                location + "/url",
                                "Provide a URL, for example https://api.example.com"));
            } else if ("/".equals(url)) {
                // swagger-parser-v3 injects [{url: "/"}] when servers is absent.
                // CONF004 already flags this case as "no servers defined", so skip here.
            } else if (url.contains("{") && url.contains("}")) {
                // Templated URL, the variables block proper URI parsing. Skip.
            } else {
                try {
                    URI uri = URI.create(url);
                    String scheme = uri.getScheme();
                    if (scheme == null
                            || !(scheme.equalsIgnoreCase("http")
                                    || scheme.equalsIgnoreCase("https"))) {
                        violations.add(
                                new Violation(
                                        id(),
                                        severity(),
                                        "Server URL '" + url + "' has invalid or missing scheme",
                                        location + "/url",
                                        "Use http:// or https:// in the server URL."));
                    }
                } catch (IllegalArgumentException e) {
                    violations.add(
                            new Violation(
                                    id(),
                                    severity(),
                                    "Server URL '" + url + "' is malformed",
                                    location + "/url",
                                    "Provide a syntactically valid URL."));
                }
            }
            idx++;
        }
        return violations;
    }
}
