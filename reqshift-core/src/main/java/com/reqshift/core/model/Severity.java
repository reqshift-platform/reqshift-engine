package com.reqshift.core.model;

public enum Severity {
    INFO(1),
    WARNING(3),
    ERROR(7),
    CRITICAL(15);

    private final int penaltyPoints;

    Severity(int penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }

    public int penaltyPoints() {
        return penaltyPoints;
    }
}
