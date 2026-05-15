package com.reqshift.core.model;

import java.util.Map;

public record Score(char grade, Map<Category, Integer> byCategory, int overall) {}
