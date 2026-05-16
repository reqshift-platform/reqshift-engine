package com.reqshift.rules.security;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

public final class RuleServersUseHttps implements Rule {

    @Override
    public String id() {
        return "SEC007";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Category category() {
        return Category.SECURITY;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        if (openApi.getServers() == null) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        int idx = 0;
        for (Server server : openApi.getServers()) {
            String url = server.getUrl();
            if (url == null
                    || url.isBlank()
                    || "/".equals(url)
                    || (url.contains("{") && url.contains("}"))) {
                idx++;
                continue;
            }
            try {
                URI uri = URI.create(url);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                boolean isLocalhost =
                        host != null
                                && (host.equals("localhost")
                                        || host.startsWith("127.")
                                        || host.equals("0.0.0.0"));
                if ("http".equalsIgnoreCase(scheme) && !isLocalhost) {
                    violations.add(
                            new Violation(
                                    id(),
                                    severity(),
                                    "Server '"
                                            + url
                                            + "' uses plain HTTP. Credentials and payloads will be transmitted in clear text.",
                                    "#/servers/" + idx + "/url",
                                    "Use https:// for any non-local server."));
                }
            } catch (IllegalArgumentException ignored) {
                // CONF005 handles malformed URLs.
            }
            idx++;
        }
        return violations;
    }
}
