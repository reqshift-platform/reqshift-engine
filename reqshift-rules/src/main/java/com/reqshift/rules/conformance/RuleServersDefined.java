package com.reqshift.rules.conformance;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleServersDefined implements Rule {

    @Override
    public String id() {
        return "CONF004";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
    }

    @Override
    public Category category() {
        return Category.CONFORMANCE;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        var servers = openApi.getServers();
        boolean noServers = servers == null || servers.isEmpty();
        boolean onlyDefaultPlaceholder =
                servers != null && servers.size() == 1 && "/".equals(servers.get(0).getUrl());
        if (noServers || onlyDefaultPlaceholder) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "No servers defined. Consumers cannot know where to call the API.",
                            "#/servers",
                            "Define at least one server, for example: servers: [{url: https://api.example.com}]"));
        }
        return List.of();
    }
}
