package io.github.dondindondev.agentprojectmemory;

import java.io.PrintWriter;

public final class Main {
  private Main() {
  }

  public static void main(String[] args) {
    var cli = new AgentProjectMemoryCli(
        new PrintWriter(System.out, true),
        new PrintWriter(System.err, true));

    int exitCode = cli.run(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }
}
