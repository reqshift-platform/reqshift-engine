package com.reqshift.core.config;

public class RuleConfigException extends RuntimeException {

    public RuleConfigException(String message) {
        super(message);
    }

    public RuleConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
