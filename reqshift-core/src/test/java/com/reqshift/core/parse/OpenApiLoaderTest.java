package com.reqshift.core.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiLoaderTest {

    @Test
    void loadsMinimalOpenApiDocument() {
        String content =
                """
                openapi: 3.0.3
                info:
                  title: Test API
                  version: 1.0.0
                paths: {}
                """;
        OpenAPI api = new OpenApiLoader().loadFromString(content);
        assertThat(api.getInfo().getTitle()).isEqualTo("Test API");
    }

    @Test
    void throwsOnInvalidContent() {
        assertThatThrownBy(() -> new OpenApiLoader().loadFromString("not yaml at all"))
                .isInstanceOf(OpenApiLoadException.class);
    }
}
