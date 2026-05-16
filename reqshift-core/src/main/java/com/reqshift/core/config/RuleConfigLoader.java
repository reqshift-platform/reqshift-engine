package com.reqshift.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reqshift.core.model.Severity;

public final class RuleConfigLoader {

    public static final String DEFAULT_FILE_NAME = ".reqshift.yml";

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public RuleConfig load(Path file) {
        if (file == null || !Files.exists(file)) {
            return RuleConfig.empty();
        }
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (isBlank(bytes)) {
                return RuleConfig.empty();
            }
            Raw raw = mapper.readValue(bytes, Raw.class);
            return toRuleConfig(raw);
        } catch (IOException e) {
            throw new RuleConfigException(
                    "Unable to read ReqShift configuration at " + file + ": " + e.getMessage(), e);
        }
    }

    private static boolean isBlank(byte[] bytes) {
        for (byte b : bytes) {
            if (!Character.isWhitespace((char) b)) {
                return false;
            }
        }
        return true;
    }

    public Optional<Path> autodetect(Path startDirectory) {
        Path dir =
                startDirectory == null ? Path.of(".").toAbsolutePath().normalize() : startDirectory;
        Path candidate = dir.resolve(DEFAULT_FILE_NAME);
        if (Files.exists(candidate)) {
            return Optional.of(candidate);
        }
        Path parent = dir.getParent();
        if (parent == null) {
            return Optional.empty();
        }
        Path parentCandidate = parent.resolve(DEFAULT_FILE_NAME);
        return Files.exists(parentCandidate) ? Optional.of(parentCandidate) : Optional.empty();
    }

    private RuleConfig toRuleConfig(Raw raw) {
        if (raw == null || raw.rules == null) {
            return RuleConfig.empty();
        }
        Set<String> disabled = new HashSet<>();
        if (raw.rules.disabled != null) {
            for (String id : raw.rules.disabled) {
                if (id != null && !id.isBlank()) {
                    disabled.add(id.trim());
                }
            }
        }
        Map<String, Severity> overrides = new HashMap<>();
        if (raw.rules.severity != null) {
            for (Map.Entry<String, String> e : raw.rules.severity.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                overrides.put(e.getKey().trim(), parseSeverity(e.getKey(), e.getValue()));
            }
        }
        return new RuleConfig(disabled, overrides);
    }

    static Severity parseSeverity(String ruleId, String raw) {
        try {
            return Severity.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuleConfigException(
                    "Invalid severity '"
                            + raw
                            + "' for rule "
                            + ruleId
                            + ". Expected one of INFO, WARNING, ERROR, CRITICAL.",
                    ex);
        }
    }

    private static final class Raw {
        public RawRules rules;
    }

    private static final class RawRules {
        public List<String> disabled;
        public Map<String, String> severity;
    }
}
