package com.reqshift.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

public final class ManifestVersionProvider implements IVersionProvider {

    private static final String POM_PROPERTIES =
            "/META-INF/maven/com.reqshift/reqshift-cli/pom.properties";

    @Override
    public String[] getVersion() {
        return new String[] {"reqshift " + currentVersion()};
    }

    public static String currentVersion() {
        try (InputStream is = ManifestVersionProvider.class.getResourceAsStream(POM_PROPERTIES)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("version", "dev");
            }
        } catch (IOException ignored) {
        }
        String fallback = ManifestVersionProvider.class.getPackage().getImplementationVersion();
        return fallback == null ? "dev" : fallback;
    }
}
