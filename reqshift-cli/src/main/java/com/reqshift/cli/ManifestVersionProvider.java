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
        try (InputStream is = getClass().getResourceAsStream(POM_PROPERTIES)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return new String[] {"reqshift " + props.getProperty("version", "unknown")};
            }
        } catch (IOException ignored) {
        }
        String fallback = getClass().getPackage().getImplementationVersion();
        return new String[] {"reqshift " + (fallback == null ? "dev" : fallback)};
    }
}
