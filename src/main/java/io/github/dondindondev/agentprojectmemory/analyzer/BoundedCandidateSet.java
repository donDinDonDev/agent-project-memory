package io.github.dondindondev.agentprojectmemory.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Function;

public final class BoundedCandidateSet<T> {
  private final int maxCandidates;
  private final Comparator<T> order;
  private final Function<T, String> keyExtractor;
  private final Map<String, T> candidates = new LinkedHashMap<>();
  private final PriorityQueue<CandidateEntry<T>> largestFirst;
  private boolean capReached;

  public BoundedCandidateSet(
      int maxCandidates,
      Comparator<T> order,
      Function<T, String> keyExtractor) {
    if (maxCandidates < 0) {
      throw new IllegalArgumentException("maxCandidates must not be negative.");
    }
    this.maxCandidates = maxCandidates;
    this.order = Objects.requireNonNull(order, "order");
    this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor");
    this.largestFirst = new PriorityQueue<>((left, right) -> compareEntries(right, left));
  }

  public void add(T candidate) {
    Objects.requireNonNull(candidate, "candidate");
    String key = Objects.requireNonNull(keyExtractor.apply(candidate), "candidate key");
    if (candidates.containsKey(key)) {
      return;
    }
    if (maxCandidates == 0) {
      capReached = true;
      return;
    }
    if (candidates.size() < maxCandidates) {
      candidates.put(key, candidate);
      largestFirst.add(new CandidateEntry<>(key, candidate));
      return;
    }

    capReached = true;
    CandidateEntry<T> largest = largestFirst.peek();
    if (largest != null && compareEntries(new CandidateEntry<>(key, candidate), largest) < 0) {
      largestFirst.poll();
      candidates.remove(largest.key());
      candidates.put(key, candidate);
      largestFirst.add(new CandidateEntry<>(key, candidate));
    }
  }

  public boolean capReached() {
    return capReached;
  }

  public List<T> sorted() {
    List<CandidateEntry<T>> sorted = new ArrayList<>();
    for (Map.Entry<String, T> entry : candidates.entrySet()) {
      sorted.add(new CandidateEntry<>(entry.getKey(), entry.getValue()));
    }
    sorted.sort(this::compareEntries);
    return sorted.stream()
        .map(CandidateEntry::candidate)
        .toList();
  }

  private int compareEntries(CandidateEntry<T> left, CandidateEntry<T> right) {
    int orderComparison = order.compare(left.candidate(), right.candidate());
    if (orderComparison != 0) {
      return orderComparison;
    }
    return left.key().compareTo(right.key());
  }

  private record CandidateEntry<T>(
      String key,
      T candidate) {
  }
}
