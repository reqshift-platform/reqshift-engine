package com.reqshift.core.model;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;

public interface Rule {

    String id();

    Severity severity();

    Category category();

    List<Violation> check(OpenAPI openApi);
}
