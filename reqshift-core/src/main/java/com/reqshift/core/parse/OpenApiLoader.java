package com.reqshift.core.parse;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public final class OpenApiLoader {

    private static final Logger log = LoggerFactory.getLogger(OpenApiLoader.class);

    public OpenAPI load(Path file) {
        log.debug("Loading OpenAPI document from {}", file);
        SwaggerParseResult result =
                new OpenAPIV3Parser().readLocation(file.toUri().toString(), null, defaultOptions());
        return validate(result, file.toString());
    }

    public OpenAPI loadFromString(String content) {
        log.debug("Loading OpenAPI document from inline content ({} chars)", content.length());
        SwaggerParseResult result =
                new OpenAPIV3Parser().readContents(content, null, defaultOptions());
        return validate(result, "<inline>");
    }

    private ParseOptions defaultOptions() {
        ParseOptions opts = new ParseOptions();
        opts.setResolve(true);
        return opts;
    }

    private OpenAPI validate(SwaggerParseResult result, String source) {
        OpenAPI api = result.getOpenAPI();
        List<String> messages = result.getMessages() == null ? List.of() : result.getMessages();
        if (api == null) {
            throw new OpenApiLoadException(
                    "Failed to parse " + source + ": " + String.join("; ", messages));
        }
        if (!messages.isEmpty()) {
            log.debug("Parser produced {} non-blocking messages for {}", messages.size(), source);
        }
        return api;
    }
}
