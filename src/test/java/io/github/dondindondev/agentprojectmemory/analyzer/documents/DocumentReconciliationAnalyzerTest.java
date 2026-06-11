package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DocumentReconciliationAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final DocumentDiscoveryAnalyzer discoveryAnalyzer = new DocumentDiscoveryAnalyzer();
  private final DocumentReconciliationAnalyzer reconciliationAnalyzer = new DocumentReconciliationAnalyzer();

  @Test
  void emitsConservativeDocumentAndSourceOnlySignalsWithStableOrdering() throws Exception {
    Path repositoryRoot = repository("signals");
    writeFile(
        repositoryRoot.resolve("README.md"),
        """
            # Root
            Documented endpoint `/orders` and ambiguous endpoint `/ambiguous`.
            Unknown endpoint `/ghost`.
            The services/orders module is documented.
            The services/payments module is not source-backed.
            billing-service module is also not source-backed.
            """);
    DocumentDiscoveryAnalysis discoveryAnalysis = discoveryAnalyzer.analyze(repositoryRoot, List.of());

    DocumentReconciliationAnalysis analysis = reconciliationAnalyzer.analyze(
        repositoryRoot,
        discoveryAnalysis,
        List.of(
            sourceApi(
                "endpoint:module:services/orders:OrdersController#list",
                "spring_mvc_endpoint",
                "module:services/orders",
                0,
                "/orders",
                List.of("/orders"),
                List.of("ev:orders")),
            sourceApi(
                "endpoint:module:services/orders:OrdersController#ambiguousOne",
                "spring_mvc_endpoint",
                "module:services/orders",
                0,
                "/ambiguous",
                List.of("/ambiguous"),
                List.of("ev:ambiguous-one")),
            sourceApi(
                "endpoint:module:services/orders:OrdersController#ambiguousTwo",
                "spring_mvc_endpoint",
                "module:services/orders",
                0,
                "/ambiguous",
                List.of("/ambiguous"),
                List.of("ev:ambiguous-two")),
            sourceApi(
                "openapi_operation:unscoped:openapi.yml:get:%2Fundocumented",
                "openapi_operation",
                null,
                Integer.MAX_VALUE,
                "GET /undocumented",
                List.of("/undocumented"),
                List.of("ev:undocumented"))),
        List.of(
            sourceModule("module:services/orders", 0, "services/orders", List.of("ev:orders-pom")),
            sourceModule("module:services/catalog", 1, "services/catalog", List.of("ev:catalog-pom"))));

    List<DocumentReconciliationSignal> signals = analysis.signals();

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(
            List.of(
                "document_only_endpoint_mention",
                "document_only_module_reference",
                "document_only_module_reference",
                "module_without_document_mention",
                "source_api_without_document_mention"),
            signals.stream().map(DocumentReconciliationSignal::signal).toList()),
        () -> assertEquals(
            List.of(
                "/ghost",
                "billing-service",
                "services/payments",
                "services/catalog",
                "GET /undocumented"),
            signals.stream().map(DocumentReconciliationSignal::subjectName).toList()),
        () -> assertEquals("endpoint_like_path", signals.get(0).subjectKind()),
        () -> assertEquals("bounded_endpoint_like_path_token", signals.get(0).matchBasis()),
        () -> assertEquals("document:README.md", signals.get(0).documentId()),
        () -> assertEquals("README.md", signals.get(0).documentPath()),
        () -> assertEquals("document_chunk:README.md:chunk:000001", signals.get(0).documentChunkId()),
        () -> assertEquals("low", signals.get(0).confidence()),
        () -> assertEquals("uncertain_signal", signals.get(0).status()),
        () -> assertEquals("bounded_module_name_token", signals.get(1).matchBasis()),
        () -> assertEquals("bounded_module_path_token", signals.get(2).matchBasis()),
        () -> assertEquals("maven_module", signals.get(3).sourceFactKind()),
        () -> assertEquals("module:services/catalog", signals.get(3).sourceFactId()),
        () -> assertEquals(List.of("ev:catalog-pom"), signals.get(3).evidenceIds()),
        () -> assertEquals("openapi_operation", signals.get(4).sourceFactKind()),
        () -> assertEquals(
            "openapi_operation:unscoped:openapi.yml:get:%2Fundocumented",
            signals.get(4).sourceFactId()),
        () -> assertEquals(List.of("ev:undocumented"), signals.get(4).evidenceIds()),
        () -> assertEquals(3, analysis.evidence().size()),
        () -> assertEquals(
            List.of("mention:/ghost", "mention:services/payments", "mention:billing-service"),
            analysis.evidence().stream().map(DocumentEvidence::symbolName).toList()),
        () -> assertEquals(
            List.of("mention token: /ghost", "mention token: services/payments",
                "mention token: billing-service"),
            analysis.evidence().stream().map(DocumentEvidence::excerpt).toList()),
        () -> assertEquals(
            List.of("low", "low", "low"),
            analysis.evidence().stream().map(DocumentEvidence::confidence).toList()),
        () -> assertFalse(signalWithSubject(signals, "/orders")),
        () -> assertFalse(signalWithSubject(signals, "/ambiguous")));
  }

  @Test
  void doesNotEmitSourceOnlySignalsWhenNoDefaultScopeDocumentExists() throws Exception {
    Path repositoryRoot = repository("no-docs");
    writeFile(repositoryRoot.resolve("notes/runbook.md"), "# Outside default scope\n");
    DocumentDiscoveryAnalysis discoveryAnalysis = discoveryAnalyzer.analyze(repositoryRoot, List.of());

    DocumentReconciliationAnalysis analysis = reconciliationAnalyzer.analyze(
        repositoryRoot,
        discoveryAnalysis,
        List.of(sourceApi(
            "endpoint:OrdersController#list",
            "spring_mvc_endpoint",
            "module:.",
            0,
            "/orders",
            List.of("/orders"),
            List.of("ev:orders"))),
        List.of(sourceModule("module:services/orders", 0, "services/orders", List.of("ev:orders-pom"))));

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.signals()),
        () -> assertEquals(List.of(), analysis.evidence()));
  }

  @Test
  void reportsNotDetectedWhenDocumentsHaveNoEligibleTokensOrSourceFacts() throws Exception {
    Path repositoryRoot = repository("no-inputs");
    writeFile(repositoryRoot.resolve("README.md"), "# Root\nPlain prose only.\n");
    DocumentDiscoveryAnalysis discoveryAnalysis = discoveryAnalyzer.analyze(repositoryRoot, List.of());

    DocumentReconciliationAnalysis analysis = reconciliationAnalyzer.analyze(
        repositoryRoot,
        discoveryAnalysis,
        List.of(),
        List.of());

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.signals()),
        () -> assertEquals(List.of(), analysis.evidence()));
  }

  @Test
  void capsAggregateMentionsSignalsAndMentionEvidence() throws Exception {
    Path repositoryRoot = repository("capped-reconciliation");
    writeFile(
        repositoryRoot.resolve("README.md"),
        """
            # Root
            Unknown endpoints: `/ghost-one`, `/ghost-two`, and `/ghost-three`.
            """);
    DocumentDiscoveryAnalysis discoveryAnalysis = discoveryAnalyzer.analyze(repositoryRoot, List.of());
    DocumentReconciliationAnalyzer cappedAnalyzer = new DocumentReconciliationAnalyzer(
        new DocumentAnalysisLimits(10, 1024, 10, 10, 2, 1));

    DocumentReconciliationAnalysis analysis = cappedAnalyzer.analyze(
        repositoryRoot,
        discoveryAnalysis,
        List.of(sourceApi(
            "endpoint:OrdersController#undocumented",
            "spring_mvc_endpoint",
            "module:.",
            0,
            "/undocumented",
            List.of("/undocumented"),
            List.of("ev:undocumented"))),
        List.of());

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(1, analysis.signals().size()),
        () -> assertEquals("/ghost-one", analysis.signals().get(0).subjectName()),
        () -> assertEquals(1, analysis.evidence().size()),
        () -> assertEquals("mention:/ghost-one", analysis.evidence().get(0).symbolName()),
        () -> assertEquals(
            List.of(
                "local_markdown_mention_count_cap_reached",
                "local_markdown_reconciliation_output_cap_reached"),
            diagnosticCodes(analysis.diagnostics())),
        () -> assertEquals(
            List.of(2, 1),
            analysis.diagnostics().stream().map(ScanDiagnostic::count).toList()),
        () -> assertFalse(signalWithSubject(analysis.signals(), "/ghost-two")),
        () -> assertFalse(signalWithSubject(analysis.signals(), "/ghost-three")),
        () -> assertFalse(signalWithSubject(analysis.signals(), "/undocumented")));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private DocumentSourceApiFact sourceApi(
      String id,
      String sourceFactKind,
      String moduleId,
      int moduleOrder,
      String subjectName,
      List<String> pathTokens,
      List<String> evidenceIds) {
    return new DocumentSourceApiFact(
        id,
        sourceFactKind,
        moduleId,
        moduleOrder,
        subjectName,
        pathTokens,
        evidenceIds);
  }

  private DocumentSourceModuleFact sourceModule(
      String moduleId,
      int moduleOrder,
      String modulePath,
      List<String> evidenceIds) {
    return new DocumentSourceModuleFact(moduleId, moduleId, moduleOrder, modulePath, evidenceIds);
  }

  private boolean signalWithSubject(List<DocumentReconciliationSignal> signals, String subjectName) {
    return signals.stream().anyMatch(signal -> subjectName.equals(signal.subjectName()));
  }

  private List<String> diagnosticCodes(List<ScanDiagnostic> diagnostics) {
    return diagnostics.stream().map(ScanDiagnostic::code).toList();
  }
}
