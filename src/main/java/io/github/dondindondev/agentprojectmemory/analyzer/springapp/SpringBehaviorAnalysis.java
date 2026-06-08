package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringBehaviorAnalysis(
    List<SpringTransactionBoundaryFact> transactionBoundaries,
    List<SpringScheduledMethodFact> scheduledMethods,
    List<SpringEventListenerFact> eventListeners,
    List<SpringMessagingListenerFact> messagingListenerSignals,
    List<SpringBehaviorEvidence> evidence) {
  public SpringBehaviorAnalysis {
    transactionBoundaries = List.copyOf(transactionBoundaries);
    scheduledMethods = List.copyOf(scheduledMethods);
    eventListeners = List.copyOf(eventListeners);
    messagingListenerSignals = List.copyOf(messagingListenerSignals);
    evidence = List.copyOf(evidence);
  }
}
