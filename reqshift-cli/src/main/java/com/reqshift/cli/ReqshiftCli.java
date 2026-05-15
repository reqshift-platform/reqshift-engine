package com.reqshift.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "reqshift",
        description =
                "Analyse an OpenAPI specification for design, security, and documentation issues.",
        mixinStandardHelpOptions = true,
        versionProvider = ManifestVersionProvider.class,
        subcommands = {AnalyzeCommand.class})
public final class ReqshiftCli implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ReqshiftCli()).execute(args);
        System.exit(exitCode);
    }
}
