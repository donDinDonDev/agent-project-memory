package io.github.dondindondev.agentprojectmemory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public final class AgentProjectMemoryCli {
  public static final String USAGE = "Usage: agent-project-memory scan <path>";
  private static final String OUTPUT_DIRECTORY_NAME = ".project-memory";
  private static final int SUCCESS = 0;
  private static final int ERROR = 2;

  private final PrintWriter out;
  private final PrintWriter err;

  public AgentProjectMemoryCli(PrintWriter out, PrintWriter err) {
    this.out = Objects.requireNonNull(out, "out");
    this.err = Objects.requireNonNull(err, "err");
  }

  public int run(String[] args) {
    if (args == null || args.length == 0) {
      return usageError("Missing command.");
    }

    if (!"scan".equals(args[0])) {
      return usageError("Unknown command: " + args[0]);
    }

    if (args.length < 2) {
      return usageError("Missing path.");
    }

    if (args.length > 2) {
      return usageError("Unexpected extra arguments.");
    }

    return scan(args[1]);
  }

  private int scan(String rawPath) {
    Path projectPath;
    try {
      projectPath = Path.of(rawPath);
    } catch (InvalidPathException ex) {
      return scanError("Invalid path: " + rawPath);
    }

    if (Files.notExists(projectPath)) {
      return scanError("Path does not exist: " + rawPath);
    }

    if (!Files.isDirectory(projectPath)) {
      return scanError("Path is not a directory: " + rawPath);
    }

    Path outputDirectory = projectPath.resolve(OUTPUT_DIRECTORY_NAME);
    if (Files.exists(outputDirectory) && !Files.isDirectory(outputDirectory)) {
      return scanError("Output path exists and is not a directory: " + outputDirectory);
    }

    try {
      Files.createDirectories(outputDirectory);
    } catch (IOException ex) {
      return scanError("Could not create output directory: " + outputDirectory);
    }

    out.println("Prepared " + outputDirectory.toAbsolutePath().normalize());
    return SUCCESS;
  }

  private int usageError(String message) {
    err.println(message);
    err.println(USAGE);
    return ERROR;
  }

  private int scanError(String message) {
    err.println(message);
    return ERROR;
  }
}
