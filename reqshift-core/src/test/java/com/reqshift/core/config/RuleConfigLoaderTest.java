package com.reqshift.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.reqshift.core.model.Severity;

class RuleConfigLoaderTest {

    @Test
    void returnsEmptyWhenFileIsNull() {
        RuleConfig cfg = new RuleConfigLoader().load(null);
        assertThat(cfg.disabled()).isEmpty();
        assertThat(cfg.severityOverrides()).isEmpty();
    }

    @Test
    void returnsEmptyWhenFileDoesNotExist(@TempDir Path tmp) {
        RuleConfig cfg = new RuleConfigLoader().load(tmp.resolve("missing.yml"));
        assertThat(cfg.disabled()).isEmpty();
        assertThat(cfg.severityOverrides()).isEmpty();
    }

    @Test
    void parsesDisabledRules(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve(".reqshift.yml");
        Files.writeString(
                file,
                """
                rules:
                  disabled: [SEC001, DES005]
                """);

        RuleConfig cfg = new RuleConfigLoader().load(file);

        assertThat(cfg.disabled()).containsExactlyInAnyOrder("SEC001", "DES005");
        assertThat(cfg.isDisabled("SEC001")).isTrue();
        assertThat(cfg.isDisabled("DOC001")).isFalse();
    }

    @Test
    void parsesSeverityOverrides(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve(".reqshift.yml");
        Files.writeString(
                file,
                """
                rules:
                  severity:
                    DES010: INFO
                    DOC004: warning
                """);

        RuleConfig cfg = new RuleConfigLoader().load(file);

        assertThat(cfg.severityOverrides())
                .containsEntry("DES010", Severity.INFO)
                .containsEntry("DOC004", Severity.WARNING);
        assertThat(cfg.severityFor("DES010", Severity.ERROR)).isEqualTo(Severity.INFO);
        assertThat(cfg.severityFor("UNKNOWN", Severity.ERROR)).isEqualTo(Severity.ERROR);
    }

    @Test
    void parsesCombinedConfig(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve(".reqshift.yml");
        Files.writeString(
                file,
                """
                rules:
                  disabled: [SEC001]
                  severity:
                    DES010: INFO
                """);

        RuleConfig cfg = new RuleConfigLoader().load(file);

        assertThat(cfg.disabled()).containsExactly("SEC001");
        assertThat(cfg.severityOverrides()).containsEntry("DES010", Severity.INFO);
    }

    @Test
    void rejectsInvalidSeverity(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve(".reqshift.yml");
        Files.writeString(
                file,
                """
                rules:
                  severity:
                    DES010: SUPER_BAD
                """);

        assertThatThrownBy(() -> new RuleConfigLoader().load(file))
                .isInstanceOf(RuleConfigException.class)
                .hasMessageContaining("SUPER_BAD")
                .hasMessageContaining("DES010");
    }

    @Test
    void autodetectFindsFileInStartDirectory(@TempDir Path tmp) throws Exception {
        Path cfg = tmp.resolve(".reqshift.yml");
        Files.writeString(cfg, "rules: {}");

        assertThat(new RuleConfigLoader().autodetect(tmp)).contains(cfg);
    }

    @Test
    void autodetectFindsFileInParentDirectory(@TempDir Path tmp) throws Exception {
        Path cfg = tmp.resolve(".reqshift.yml");
        Files.writeString(cfg, "rules: {}");
        Path child = Files.createDirectory(tmp.resolve("child"));

        assertThat(new RuleConfigLoader().autodetect(child)).contains(cfg);
    }

    @Test
    void autodetectReturnsEmptyWhenAbsent(@TempDir Path tmp) {
        assertThat(new RuleConfigLoader().autodetect(tmp)).isEmpty();
    }

    @Test
    void handlesEmptyDocument(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve(".reqshift.yml");
        Files.writeString(file, "");

        RuleConfig cfg = new RuleConfigLoader().load(file);

        assertThat(cfg.disabled()).isEmpty();
        assertThat(cfg.severityOverrides()).isEmpty();
    }
}
