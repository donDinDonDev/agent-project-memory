package io.github.dondindondev.agentprojectmemory.analyzer.build;

import java.util.List;

public interface BuildModule {
  String moduleId();

  String modulePath();

  List<String> sourceRoots();

  List<String> testRoots();

  String supportStatus();
}
