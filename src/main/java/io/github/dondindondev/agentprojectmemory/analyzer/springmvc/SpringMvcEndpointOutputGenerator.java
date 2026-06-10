package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.ApiSpecEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiOperationAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiOperationAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiOperationFact;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiSpecDiscoveryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiSpecDiscoveryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiSpecFileFact;
import io.github.dondindondev.agentprojectmemory.analyzer.apisurface.OpenApiSpecWarningFact;
import io.github.dondindondev.agentprojectmemory.analyzer.config.ConfigFileFact;
import io.github.dondindondev.agentprojectmemory.analyzer.config.ModuleResourceConfig;
import io.github.dondindondev.agentprojectmemory.analyzer.config.ResourceConfigAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.config.ResourceConfigAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.config.ResourceConfigEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.config.ResourceRootFact;
import io.github.dondindondev.agentprojectmemory.analyzer.documents.DocumentDiscoveryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.documents.DocumentDiscoveryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.documents.DocumentDiscoveryPolicy;
import io.github.dondindondev.agentprojectmemory.analyzer.documents.DocumentFileFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEmbeddableFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEmbeddedFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityFieldFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaIdClassFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaColumnFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEnumeratedFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaGeneratedValueFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaIdentifierFieldFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaJoinColumnFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaJoinTableFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaRelationshipFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaRelationshipTargetFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaVersionFact;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyDeclaration;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDiscoveryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDiscoveryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDiscoveryEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDependencies;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleMetadata;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleWarning;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataParent;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataValue;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModulePlugins;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPluginAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPluginAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPluginDeclaration;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPluginEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPluginExecution;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPluginSignal;
import io.github.dondindondev.agentprojectmemory.analyzer.springboot.ModuleSpringBootApplications;
import io.github.dondindondev.agentprojectmemory.analyzer.springboot.SpringBootApplicationAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.springboot.SpringBootApplicationAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.springboot.SpringBootApplicationEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.springboot.SpringBootApplicationFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringBeanMethodFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringBehaviorAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringBehaviorAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringBehaviorEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringConfigurationAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringConfigurationAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringConfigurationClassFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringConfigurationEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringConfigurationPropertiesFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringEventListenerFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringMessagingListenerFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringRepositoryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringRepositoryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringRepositoryEntityGenericFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringRepositoryEntityRelationFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringRepositoryEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringRepositoryFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringScheduledMethodFact;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringSecurityConfigurationAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.springapp.SpringTransactionBoundaryFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestClassFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestFrameworkSignalFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestInventoryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestInventoryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestInventoryEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestMethodFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestMockSignalFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestSpringSliceFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestedSubjectFact;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningFact;
import io.github.dondindondev.agentprojectmemory.generator.AgentGuideGenerator;
import io.github.dondindondev.agentprojectmemory.generator.MarkdownRenderer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public final class SpringMvcEndpointOutputGenerator {
  private static final String MAIN_SOURCE_ROOT = "src/main/java";
  private static final String TEST_SOURCE_ROOT = "src/test/java";
  private static final String MAIN_RESOURCE_ROOT = "src/main/resources";
  private static final String TEST_RESOURCE_ROOT = "src/test/resources";
  private static final String ROOT_BUILD_FILE = "pom.xml";
  private static final String SCHEMA_VERSION = "0.8";
  private static final String ANALYSIS_ANALYZED = "analyzed";
  private static final String ANALYSIS_NOT_ANALYZED = "not_analyzed";
  private static final String ANALYSIS_NOT_DETECTED = "not_detected";
  private static final String MODULE_ANALYSIS_NOT_DETECTED = "not_detected";
  private static final String MODULE_SUPPORTED = "supported";
  private static final String ROOT_MODULE_ID = "module:.";
  private static final String QUALITY_STATUS_NO_OBVIOUS_TEST = "no_obvious_test";
  private static final String QUALITY_STATUS_PLANNING_HINT = "planning_hint";
  private static final String QUALITY_STATUS_WARNING_ORIENTED_PLANNING_HINT =
      "warning_oriented_planning_hint";
  private static final String QUALITY_STATUS_UNCERTAIN_PLANNING_HINT =
      "uncertain_planning_hint";
  private static final String QUALITY_UNCERTAINTY_BOUNDED_TEST_INVENTORY =
      "bounded_test_inventory_supported_relations_only";
  private static final String QUALITY_UNCERTAINTY_TEST_ROOTS_NOT_DETECTED =
      "supported_test_roots_not_detected";
  private static final String QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY =
      "source_visible_change_surface_only";
  private static final String PROJECT_MAP_FILE_NAME = "project-map.json";
  private static final String ENDPOINTS_FILE_NAME = "endpoints.md";
  private static final String EVIDENCE_INDEX_FILE_NAME = "evidence-index.jsonl";
  private static final String AGENT_GUIDE_FILE_NAME = "agent-guide.md";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String WARNING_CATEGORY_GENERATED_SOURCE = "generated_source";
  private static final String WARNING_SIGNAL_MAVEN_GENERATOR_PLUGIN = "maven_generator_plugin";
  private static final String WARNING_SIGNAL_MAVEN_OPENAPI_SWAGGER_CODEGEN_PLUGIN =
      "maven_openapi_swagger_codegen_plugin";
  private static final String WARNING_SIGNAL_MAVEN_ANNOTATION_PROCESSOR =
      "maven_annotation_processor";
  private static final String WARNING_SIGNAL_MAVEN_GENERATED_SOURCE_CONFIG =
      "maven_generated_source_config";
  private static final String WARNING_SIGNAL_MAVEN_BUILD_HELPER_ADD_SOURCE =
      "maven_build_helper_add_source";
  private static final String PLUGIN_SIGNAL_OPENAPI_SWAGGER_CODEGEN = "openapi_swagger_codegen";
  private static final String PLUGIN_SIGNAL_SOURCE_GENERATOR_PLUGIN = "source_generator_plugin";
  private static final String PLUGIN_SIGNAL_ANNOTATION_PROCESSOR = "annotation_processor";
  private static final String CONFIG_SIGNAL_GENERATED_SOURCES_CONFIG_PRESENT =
      "generated_sources_config_present";
  private static final String CONFIG_SIGNAL_ADD_SOURCE_GOAL_PRESENT = "add_source_goal_present";
  private static final String API_SURFACE_CATEGORY_SOURCE_VISIBLE =
      "source_visible_spring_mvc_endpoint";
  private static final String API_SURFACE_CATEGORY_INTERFACE_DECLARED =
      "interface_declared_spring_mvc_endpoint";
  private static final String WARNING_CATEGORY_HIDDEN_HTTP_SURFACE = "hidden_http_surface";
  private static final String WARNING_SIGNAL_OPENAPI_SPEC_FILE = "openapi_spec_file";
  private static final String WARNING_SIGNAL_REPOSITORY_REST_RESOURCE = "repository_rest_resource";
  private static final String WARNING_CATEGORY_SPRING_SECURITY = "spring_security";
  private static final String WARNING_SIGNAL_GENERATED_SOURCE_ROOT_PATH_DETECTED =
      "generated_source_root_path_detected";
  private static final Comparator<EvidenceRecord> EVIDENCE_ORDER = Comparator
      .comparing(EvidenceRecord::path)
      .thenComparing(record -> record.lineStart() == null ? Integer.MAX_VALUE : record.lineStart())
      .thenComparing(record -> record.lineEnd() == null ? Integer.MAX_VALUE : record.lineEnd())
      .thenComparing(record -> nullSafe(record.className()))
      .thenComparing(record -> nullSafe(record.methodName()))
      .thenComparing(EvidenceRecord::symbolName)
      .thenComparing(EvidenceRecord::id);
  private static final Comparator<ModuleScopedEndpointFact> ENDPOINT_ORDER = Comparator
      .comparingInt(ModuleScopedEndpointFact::moduleOrder)
      .thenComparing(endpoint -> firstPath(endpoint.fact()))
      .thenComparing(endpoint -> String.join(",", endpoint.fact().httpMethods()))
      .thenComparing(endpoint -> endpoint.fact().httpMethodSemantics().name())
      .thenComparing(endpoint -> endpoint.fact().controllerClass())
      .thenComparing(endpoint -> endpoint.fact().handlerMethod());
  private static final Comparator<ModuleScopedComponentFact> COMPONENT_ORDER = Comparator
      .comparingInt(ModuleScopedComponentFact::moduleOrder)
      .thenComparing(component -> component.fact().className())
      .thenComparing(component -> componentId(component.moduleId(), component.fact()));
  private static final Comparator<ModuleScopedSpringRepositoryFact> SPRING_REPOSITORY_ORDER = Comparator
      .comparingInt(ModuleScopedSpringRepositoryFact::moduleOrder)
      .thenComparing(repository -> repository.fact().sourcePath())
      .thenComparing(repository -> repository.fact().className())
      .thenComparing(repository -> repository.fact().surfaceCategory())
      .thenComparing(repository -> springRepositoryId(repository.moduleId(), repository.fact()));
  private static final Comparator<ModuleScopedSpringConfigurationClassFact> SPRING_CONFIGURATION_CLASS_ORDER =
      Comparator
          .comparingInt(ModuleScopedSpringConfigurationClassFact::moduleOrder)
          .thenComparing(configuration -> configuration.fact().sourcePath())
          .thenComparing(configuration -> configuration.fact().className())
          .thenComparing(configuration -> springConfigurationClassId(
              configuration.moduleId(),
              configuration.fact()));
  private static final Comparator<ModuleScopedSpringConfigurationPropertiesFact>
      SPRING_CONFIGURATION_PROPERTIES_ORDER =
          Comparator
              .comparingInt(ModuleScopedSpringConfigurationPropertiesFact::moduleOrder)
              .thenComparing(properties -> properties.fact().sourcePath())
              .thenComparing(properties -> properties.fact().className())
              .thenComparing(properties -> springConfigurationPropertiesId(
                  properties.moduleId(),
                  properties.fact()));
  private static final Comparator<ModuleScopedSpringBeanMethodFact> SPRING_BEAN_METHOD_ORDER = Comparator
      .comparingInt(ModuleScopedSpringBeanMethodFact::moduleOrder)
      .thenComparing(beanMethod -> beanMethod.fact().sourcePath())
      .thenComparing(beanMethod -> beanMethod.fact().className())
      .thenComparing(beanMethod -> beanMethod.fact().methodName())
      .thenComparing(beanMethod -> springBeanMethodId(beanMethod.moduleId(), beanMethod.fact()));
  private static final Comparator<ModuleScopedSpringTransactionBoundaryFact>
      SPRING_TRANSACTION_BOUNDARY_ORDER =
          Comparator
              .comparingInt(ModuleScopedSpringTransactionBoundaryFact::moduleOrder)
              .thenComparing(boundary -> boundary.fact().sourcePath())
              .thenComparing(boundary -> boundary.fact().className())
              .thenComparing(boundary -> nullSafe(boundary.fact().methodName()))
              .thenComparing(boundary -> boundary.fact().targetKind())
              .thenComparing(boundary -> springTransactionBoundaryId(
                  boundary.moduleId(),
                  boundary.fact()));
  private static final Comparator<ModuleScopedSpringScheduledMethodFact>
      SPRING_SCHEDULED_METHOD_ORDER =
          Comparator
              .comparingInt(ModuleScopedSpringScheduledMethodFact::moduleOrder)
              .thenComparing(scheduledMethod -> scheduledMethod.fact().sourcePath())
              .thenComparing(scheduledMethod -> scheduledMethod.fact().className())
              .thenComparing(scheduledMethod -> scheduledMethod.fact().methodName())
              .thenComparing(scheduledMethod -> springScheduledMethodId(
                  scheduledMethod.moduleId(),
                  scheduledMethod.fact()));
  private static final Comparator<ModuleScopedSpringEventListenerFact>
      SPRING_EVENT_LISTENER_ORDER =
          Comparator
              .comparingInt(ModuleScopedSpringEventListenerFact::moduleOrder)
              .thenComparing(eventListener -> eventListener.fact().sourcePath())
              .thenComparing(eventListener -> eventListener.fact().className())
              .thenComparing(eventListener -> eventListener.fact().methodName())
              .thenComparing(eventListener -> springEventListenerId(
                  eventListener.moduleId(),
                  eventListener.fact()));
  private static final Comparator<ModuleScopedSpringMessagingListenerFact>
      SPRING_MESSAGING_LISTENER_ORDER =
          Comparator
              .comparingInt(ModuleScopedSpringMessagingListenerFact::moduleOrder)
              .thenComparing(listener -> listener.fact().sourcePath())
              .thenComparing(listener -> listener.fact().className())
              .thenComparing(listener -> nullSafe(listener.fact().methodName()))
              .thenComparing(listener -> listener.fact().targetKind())
              .thenComparing(listener -> listener.fact().listenerFramework())
              .thenComparing(listener -> listener.fact().annotationSymbol())
              .thenComparing(listener -> springMessagingListenerId(
                  listener.moduleId(),
                  listener.fact()));
  private static final Comparator<ModuleScopedEntityFact> ENTITY_ORDER = Comparator
      .comparingInt(ModuleScopedEntityFact::moduleOrder)
      .thenComparing(entity -> entity.fact().className())
      .thenComparing(entity -> entityId(entity.moduleId(), entity.fact()));
  private static final Comparator<ModuleScopedEmbeddableFact> EMBEDDABLE_ORDER = Comparator
      .comparingInt(ModuleScopedEmbeddableFact::moduleOrder)
      .thenComparing(embeddable -> embeddable.fact().className())
      .thenComparing(embeddable -> embeddable.fact().sourcePath())
      .thenComparing(embeddable -> embeddableId(embeddable.moduleId(), embeddable.fact()));
  private static final Comparator<JpaIdentifierFieldFact> IDENTIFIER_FIELD_ORDER = Comparator
      .comparing(JpaIdentifierFieldFact::sourceKind)
      .thenComparing(JpaIdentifierFieldFact::declaringClass)
      .thenComparing(JpaIdentifierFieldFact::fieldName)
      .thenComparing(JpaIdentifierFieldFact::javaType)
      .thenComparing(JpaIdentifierFieldFact::identifierKind);
  private static final Comparator<JpaRelationshipFact> RELATIONSHIP_ORDER = Comparator
      .comparing(JpaRelationshipFact::fieldName)
      .thenComparing(JpaRelationshipFact::cardinality)
      .thenComparing(JpaRelationshipFact::annotation)
      .thenComparing(JpaRelationshipFact::javaType);
  private static final Comparator<JpaEntityFieldFact> ENTITY_FIELD_ORDER = Comparator
      .comparing(JpaEntityFieldFact::declaringClass)
      .thenComparing(JpaEntityFieldFact::sourceKind)
      .thenComparing(JpaEntityFieldFact::fieldName)
      .thenComparing(JpaEntityFieldFact::javaType)
      .thenComparing(JpaEntityFieldFact::persistenceRole);
  private static final Comparator<ModuleScopedTestFact> TEST_CLASS_ORDER = Comparator
      .comparingInt(ModuleScopedTestFact::moduleOrder)
      .thenComparing(test -> test.fact().className())
      .thenComparing(test -> test.fact().sourcePath());
  private static final Comparator<TestFrameworkSignalFact> TEST_FRAMEWORK_SIGNAL_ORDER = Comparator
      .comparing(TestFrameworkSignalFact::name);
  private static final Comparator<TestSpringSliceFact> TEST_SPRING_SLICE_ORDER = Comparator
      .comparing(TestSpringSliceFact::sliceKind)
      .thenComparing(TestSpringSliceFact::annotation)
      .thenComparing(slice -> String.join("\n", slice.evidenceIds()));
  private static final Comparator<TestMockSignalFact> TEST_MOCK_SIGNAL_ORDER = Comparator
      .comparing(TestMockSignalFact::targetKind)
      .thenComparing(TestMockSignalFact::targetName)
      .thenComparing(TestMockSignalFact::mockSignal)
      .thenComparing(TestMockSignalFact::annotation)
      .thenComparing(signal -> String.join("\n", signal.evidenceIds()));
  private static final Comparator<TestedSubjectFact> TESTED_SUBJECT_ORDER = Comparator
      .comparing(TestedSubjectFact::relationStatus)
      .thenComparing(TestedSubjectFact::relationType)
      .thenComparing(subject -> nullSafe(subject.className()))
      .thenComparing(subject -> nullSafe(subject.candidateReference()))
      .thenComparing(subject -> nullSafe(subject.supportType()))
      .thenComparing(TestedSubjectFact::confidence)
      .thenComparing(subject -> nullSafe(subject.uncertainty()));
  private static final Comparator<ModuleScopedWarningFact> WARNING_ORDER = Comparator
      .comparing(ModuleScopedWarningFact::category)
      .thenComparing(ModuleScopedWarningFact::signal)
      .thenComparingInt(ModuleScopedWarningFact::moduleOrder)
      .thenComparing(ModuleScopedWarningFact::sourcePath)
      .thenComparing(ModuleScopedWarningFact::id);
  private static final Comparator<TestGapSignal> TEST_GAP_SIGNAL_ORDER = Comparator
      .comparingInt(TestGapSignal::moduleOrder)
      .thenComparing(TestGapSignal::subjectKind)
      .thenComparing(TestGapSignal::subjectName)
      .thenComparing(TestGapSignal::subjectId);
  private static final Comparator<ChangeRiskSignal> CHANGE_RISK_SIGNAL_ORDER = Comparator
      .comparingInt(ChangeRiskSignal::moduleOrder)
      .thenComparing(ChangeRiskSignal::signal)
      .thenComparing(ChangeRiskSignal::subjectKind)
      .thenComparing(ChangeRiskSignal::subjectName)
      .thenComparing(ChangeRiskSignal::subjectId);
  private static final Comparator<ResourceRootFact> RESOURCE_ROOT_ORDER = Comparator
      .comparing(ResourceRootFact::scope)
      .thenComparing(ResourceRootFact::path)
      .thenComparing(ResourceRootFact::id);
  private static final Comparator<ConfigFileFact> CONFIG_FILE_ORDER = Comparator
      .comparing(ConfigFileFact::resourceScope)
      .thenComparing(ConfigFileFact::configKind)
      .thenComparing(ConfigFileFact::path)
      .thenComparing(ConfigFileFact::id);
  private static final Comparator<SpringBootApplicationFact> SPRING_BOOT_APPLICATION_ORDER = Comparator
      .comparing(SpringBootApplicationFact::className)
      .thenComparing(SpringBootApplicationFact::sourcePath)
      .thenComparing(SpringBootApplicationFact::id);

  private final SpringMvcEndpointAnalyzer analyzer;
  private final SpringComponentAnalyzer componentAnalyzer;
  private final JpaEntityAnalyzer entityAnalyzer;
  private final TestInventoryAnalyzer testInventoryAnalyzer;
  private final AnalysisWarningAnalyzer warningAnalyzer;
  private final MavenModuleDiscoveryAnalyzer moduleDiscoveryAnalyzer;
  private final MavenMetadataAnalyzer mavenMetadataAnalyzer;
  private final MavenDependencyAnalyzer mavenDependencyAnalyzer;
  private final MavenPluginAnalyzer mavenPluginAnalyzer = new MavenPluginAnalyzer();
  private final ResourceConfigAnalyzer resourceConfigAnalyzer = new ResourceConfigAnalyzer();
  private final SpringBootApplicationAnalyzer springBootApplicationAnalyzer =
      new SpringBootApplicationAnalyzer();
  private final SpringRepositoryAnalyzer springRepositoryAnalyzer = new SpringRepositoryAnalyzer();
  private final SpringConfigurationAnalyzer springConfigurationAnalyzer =
      new SpringConfigurationAnalyzer();
  private final SpringBehaviorAnalyzer springBehaviorAnalyzer = new SpringBehaviorAnalyzer();
  private final SpringSecurityConfigurationAnalyzer springSecurityConfigurationAnalyzer =
      new SpringSecurityConfigurationAnalyzer();
  private final OpenApiSpecDiscoveryAnalyzer openApiSpecDiscoveryAnalyzer =
      new OpenApiSpecDiscoveryAnalyzer();
  private final OpenApiOperationAnalyzer openApiOperationAnalyzer =
      new OpenApiOperationAnalyzer();
  private final DocumentDiscoveryAnalyzer documentDiscoveryAnalyzer = new DocumentDiscoveryAnalyzer();
  private final AgentGuideGenerator agentGuideGenerator;

  public SpringMvcEndpointOutputGenerator() {
    this(
        new SpringMvcEndpointAnalyzer(),
        new SpringComponentAnalyzer(),
        new JpaEntityAnalyzer(),
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(SpringMvcEndpointAnalyzer analyzer) {
    this(
        analyzer,
        new SpringComponentAnalyzer(),
        new JpaEntityAnalyzer(),
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer) {
    this(
        analyzer,
        componentAnalyzer,
        new JpaEntityAnalyzer(),
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer,
      JpaEntityAnalyzer entityAnalyzer) {
    this(
        analyzer,
        componentAnalyzer,
        entityAnalyzer,
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer,
      JpaEntityAnalyzer entityAnalyzer,
      TestInventoryAnalyzer testInventoryAnalyzer) {
    this(
        analyzer,
        componentAnalyzer,
        entityAnalyzer,
        testInventoryAnalyzer,
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer,
      JpaEntityAnalyzer entityAnalyzer,
      TestInventoryAnalyzer testInventoryAnalyzer,
      AnalysisWarningAnalyzer warningAnalyzer,
      MavenModuleDiscoveryAnalyzer moduleDiscoveryAnalyzer,
      MavenMetadataAnalyzer mavenMetadataAnalyzer,
      MavenDependencyAnalyzer mavenDependencyAnalyzer,
      AgentGuideGenerator agentGuideGenerator) {
    this.analyzer = Objects.requireNonNull(analyzer, "analyzer");
    this.componentAnalyzer = Objects.requireNonNull(componentAnalyzer, "componentAnalyzer");
    this.entityAnalyzer = Objects.requireNonNull(entityAnalyzer, "entityAnalyzer");
    this.testInventoryAnalyzer = Objects.requireNonNull(
        testInventoryAnalyzer,
        "testInventoryAnalyzer");
    this.warningAnalyzer = Objects.requireNonNull(warningAnalyzer, "warningAnalyzer");
    this.moduleDiscoveryAnalyzer = Objects.requireNonNull(
        moduleDiscoveryAnalyzer,
        "moduleDiscoveryAnalyzer");
    this.mavenMetadataAnalyzer = Objects.requireNonNull(
        mavenMetadataAnalyzer,
        "mavenMetadataAnalyzer");
    this.mavenDependencyAnalyzer = Objects.requireNonNull(
        mavenDependencyAnalyzer,
        "mavenDependencyAnalyzer");
    this.agentGuideGenerator = Objects.requireNonNull(agentGuideGenerator, "agentGuideGenerator");
  }

  public Result generate(Path repositoryRoot, Path outputDirectory) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(outputDirectory, "outputDirectory");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis = moduleDiscoveryAnalyzer.analyze(
        normalizedRepositoryRoot);
    ProjectLayout layout = detectLayout(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        moduleDiscoveryAnalysis);
    MavenMetadataAnalysis metadataAnalysis = mavenMetadataAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    MavenDependencyAnalysis dependencyAnalysis = mavenDependencyAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    MavenPluginAnalysis pluginAnalysis = mavenPluginAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    ResourceConfigAnalysis resourceConfigAnalysis = resourceConfigAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    SpringBootApplicationAnalysis springBootApplicationAnalysis = springBootApplicationAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    OpenApiSpecDiscoveryAnalysis openApiSpecDiscoveryAnalysis = openApiSpecDiscoveryAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    OpenApiOperationAnalysis openApiOperationAnalysis = openApiOperationAnalyzer.analyze(
        normalizedRepositoryRoot,
        openApiSpecDiscoveryAnalysis.specFiles());
    DocumentDiscoveryAnalysis documentDiscoveryAnalysis = documentDiscoveryAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    if (!shouldGenerate(
        layout,
        moduleDiscoveryAnalysis,
        metadataAnalysis,
        dependencyAnalysis,
        pluginAnalysis,
        resourceConfigAnalysis,
        springBootApplicationAnalysis,
        openApiSpecDiscoveryAnalysis,
        openApiOperationAnalysis,
        documentDiscoveryAnalysis)) {
      return new Result(false, 0, 0, 0, 0, 0, 0);
    }

    ModuleAwareScan scan = analyzeModules(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        layout.modules(),
        moduleDiscoveryAnalysis.warnings(),
        pluginAnalysis,
        openApiOperationAnalysis);
    List<EvidenceRecord> evidenceRecords = evidenceRecords(
        layout,
        moduleDiscoveryAnalysis.evidence(),
        metadataAnalysis.evidence(),
        dependencyAnalysis.evidence(),
        pluginAnalysis.evidence(),
        resourceConfigAnalysis.evidence(),
        springBootApplicationAnalysis.evidence(),
        openApiSpecDiscoveryAnalysis.evidence(),
        openApiOperationAnalysis.evidence(),
        scan.endpointEvidence(),
        scan.componentEvidence(),
        scan.springRepositoryEvidence(),
        scan.springConfigurationEvidence(),
        scan.springBehaviorEvidence(),
        scan.entityEvidence(),
        scan.testEvidence(),
        scan.warningEvidence());
    String evidenceIndexJsonl = evidenceIndexJsonl(evidenceRecords);
    String projectMapJson = projectMapJson(
        layout,
        scan,
        metadataAnalysis,
        dependencyAnalysis,
        pluginAnalysis,
        resourceConfigAnalysis,
        springBootApplicationAnalysis,
        openApiSpecDiscoveryAnalysis,
        openApiOperationAnalysis,
        documentDiscoveryAnalysis);

    writeGeneratedFiles(
        canonicalRepositoryRoot,
        outputDirectory,
        List.of(
            new GeneratedOutputFile(
                ENDPOINTS_FILE_NAME,
                endpointsMarkdown(
                    layout.modules(),
                    scan.endpoints(),
                    openApiOperationAnalysis.operations(),
                    scan.warnings())),
            new GeneratedOutputFile(
                EVIDENCE_INDEX_FILE_NAME,
                evidenceIndexJsonl),
            new GeneratedOutputFile(
                PROJECT_MAP_FILE_NAME,
                projectMapJson),
            new GeneratedOutputFile(
                AGENT_GUIDE_FILE_NAME,
                agentGuideGenerator.generate(projectMapJson, evidenceIndexJsonl))));

    return new Result(
        true,
        scan.endpoints().size(),
        scan.components().size(),
        scan.entities().size(),
        scan.tests().size(),
        documentDiscoveryAnalysis.documents().size(),
        evidenceRecords.size());
  }

  private boolean shouldGenerate(
      ProjectLayout layout,
      MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis,
      MavenMetadataAnalysis metadataAnalysis,
      MavenDependencyAnalysis dependencyAnalysis,
      MavenPluginAnalysis pluginAnalysis,
      ResourceConfigAnalysis resourceConfigAnalysis,
      SpringBootApplicationAnalysis springBootApplicationAnalysis,
      OpenApiSpecDiscoveryAnalysis openApiSpecDiscoveryAnalysis,
      OpenApiOperationAnalysis openApiOperationAnalysis,
      DocumentDiscoveryAnalysis documentDiscoveryAnalysis) {
    return !layout.sourceRoots().isEmpty()
        || !layout.testRoots().isEmpty()
        || !moduleDiscoveryAnalysis.warnings().isEmpty()
        || metadataAnalysis.modules().stream()
            .anyMatch(metadata -> ANALYSIS_ANALYZED.equals(metadata.analysisStatus()))
        || dependencyAnalysis.modules().stream()
            .anyMatch(dependencies -> ANALYSIS_ANALYZED.equals(dependencies.analysisStatus()))
        || pluginAnalysis.modules().stream()
            .anyMatch(plugins -> ANALYSIS_ANALYZED.equals(plugins.analysisStatus()))
        || resourceConfigAnalysis.modules().stream()
            .anyMatch(resources -> ANALYSIS_ANALYZED.equals(resources.resourceAnalysisStatus())
                || ANALYSIS_ANALYZED.equals(resources.configFileAnalysisStatus()))
        || springBootApplicationAnalysis.modules().stream()
            .anyMatch(applications -> ANALYSIS_ANALYZED.equals(applications.analysisStatus()))
        || !openApiSpecDiscoveryAnalysis.specFiles().isEmpty()
        || !openApiOperationAnalysis.operations().isEmpty()
        || !openApiOperationAnalysis.warnings().isEmpty()
        || !documentDiscoveryAnalysis.documents().isEmpty();
  }

  private ProjectLayout detectLayout(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis) throws IOException {
    Optional<EvidenceRecord> buildFileEvidence = buildFileEvidence(
        repositoryRoot,
        canonicalRepositoryRoot);
    BuildMetadata build = buildFileEvidence
        .map(evidence -> new BuildMetadata("maven", ROOT_BUILD_FILE, List.of(evidence.id())))
        .orElseGet(() -> new BuildMetadata("not_detected", null, List.of()));

    ProjectModules modules = projectModules(
        repositoryRoot,
        canonicalRepositoryRoot,
        moduleDiscoveryAnalysis);
    List<String> sourceRoots = modules.items().stream()
        .flatMap(module -> module.sourceRoots().stream())
        .sorted()
        .toList();
    List<String> testRoots = modules.items().stream()
        .flatMap(module -> module.testRoots().stream())
        .sorted()
        .toList();

    return new ProjectLayout(build, sourceRoots, testRoots, modules, buildFileEvidence);
  }

  private ProjectModules projectModules(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis) {
    if (!MODULE_ANALYSIS_NOT_DETECTED.equals(moduleDiscoveryAnalysis.analysisStatus())) {
      return new ProjectModules(moduleDiscoveryAnalysis.analysisStatus(), moduleDiscoveryAnalysis.items());
    }

    List<String> sourceRoots = detectedRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        List.of(MAIN_SOURCE_ROOT));
    List<String> testRoots = detectedRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        List.of(TEST_SOURCE_ROOT));
    List<String> resourceRoots = detectedRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        List.of(MAIN_RESOURCE_ROOT, TEST_RESOURCE_ROOT));
    if (sourceRoots.isEmpty() && testRoots.isEmpty() && resourceRoots.isEmpty()) {
      return new ProjectModules(MODULE_ANALYSIS_NOT_DETECTED, List.of());
    }

    return new ProjectModules(
        MODULE_ANALYSIS_NOT_DETECTED,
        List.of(new MavenModuleItem(
            ROOT_MODULE_ID,
            ".",
            null,
            sourceRoots,
            testRoots,
            MODULE_SUPPORTED,
            "scan_root",
            ".",
            List.of(),
            List.of())));
  }

  private ModuleAwareScan analyzeModules(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      ProjectModules modules,
      List<MavenModuleWarning> moduleWarnings,
      MavenPluginAnalysis pluginAnalysis,
      OpenApiOperationAnalysis openApiOperationAnalysis) throws IOException {
    List<ModuleScopedEndpointFact> endpoints = new ArrayList<>();
    List<ModuleScopedComponentFact> components = new ArrayList<>();
    List<ModuleScopedSpringRepositoryFact> springRepositories = new ArrayList<>();
    List<ModuleScopedEntityFact> entities = new ArrayList<>();
    List<ModuleScopedTestFact> tests = new ArrayList<>();
    List<ModuleScopedWarningFact> warnings = new ArrayList<>();
    List<SpringMvcEndpointEvidence> endpointEvidence = new ArrayList<>();
    List<SpringComponentEvidence> componentEvidence = new ArrayList<>();
    List<SpringRepositoryEvidence> springRepositoryEvidence = new ArrayList<>();
    List<ModuleScopedSpringConfigurationClassFact> springConfigurationClasses = new ArrayList<>();
    List<ModuleScopedSpringConfigurationPropertiesFact> springConfigurationProperties = new ArrayList<>();
    List<ModuleScopedSpringBeanMethodFact> springBeanMethods = new ArrayList<>();
    List<SpringConfigurationEvidence> springConfigurationEvidence = new ArrayList<>();
    List<ModuleScopedSpringTransactionBoundaryFact> springTransactionBoundaries = new ArrayList<>();
    List<ModuleScopedSpringScheduledMethodFact> springScheduledMethods = new ArrayList<>();
    List<ModuleScopedSpringEventListenerFact> springEventListeners = new ArrayList<>();
    List<ModuleScopedSpringMessagingListenerFact> springMessagingListeners = new ArrayList<>();
    List<SpringBehaviorEvidence> springBehaviorEvidence = new ArrayList<>();
    List<ModuleScopedEmbeddableFact> embeddables = new ArrayList<>();
    List<JpaEntityEvidence> entityEvidence = new ArrayList<>();
    List<TestInventoryEvidence> testEvidence = new ArrayList<>();
    List<AnalysisWarningEvidence> warningEvidence = new ArrayList<>();
    Map<String, Integer> moduleOrder = moduleOrder(modules.items());
    boolean componentAnalyzerRan = false;
    boolean springRepositoryAnalyzerRan = false;
    boolean springConfigurationAnalyzerRan = false;
    boolean springBehaviorAnalyzerRan = false;
    boolean springSecurityAnalyzerRan = false;
    boolean entityAnalyzerRan = false;
    boolean testAnalyzerRan = false;

    for (MavenModuleWarning warning : moduleWarnings) {
      warnings.add(new ModuleScopedWarningFact(
          warning.id(),
          warning.category(),
          warning.signal(),
          warning.moduleId(),
          moduleOrder.getOrDefault(warning.moduleId(), Integer.MAX_VALUE),
          warning.message(),
          warning.sourcePath(),
          warning.evidenceIds()));
    }
    List<ModuleScopedWarningFact> pluginWarnings = generatedSourceWarnings(
        pluginAnalysis,
        moduleOrder,
        modules.items());
    warnings.addAll(pluginWarnings);
    for (OpenApiSpecWarningFact warning : openApiOperationAnalysis.warnings()) {
      warnings.add(new ModuleScopedWarningFact(
          warning.id(),
          warning.category(),
          warning.signal(),
          warning.moduleId(),
          warning.moduleOrder(),
          warning.message(),
          warning.sourcePath(),
          warning.evidenceIds()));
    }
    boolean warningAnalyzerRan = !moduleWarnings.isEmpty()
        || !pluginWarnings.isEmpty()
        || !openApiOperationAnalysis.warnings().isEmpty();

    List<String> childModulePaths = modules.items().stream()
        .map(MavenModuleItem::modulePath)
        .filter(modulePath -> !".".equals(modulePath))
        .toList();

    for (MavenModuleItem module : modules.items()) {
      if (!MODULE_SUPPORTED.equals(module.supportStatus())) {
        continue;
      }

      int order = moduleOrder.getOrDefault(module.moduleId(), Integer.MAX_VALUE);
      List<Path> sourceRoots = module.sourceRoots().stream()
          .map(repositoryRoot::resolve)
          .filter(sourceRoot -> ScanPathContainment.isDirectoryUnderRoot(
              canonicalRepositoryRoot,
              sourceRoot))
          .toList();
      List<Path> testRoots = module.testRoots().stream()
          .map(repositoryRoot::resolve)
          .filter(testRoot -> ScanPathContainment.isDirectoryUnderRoot(
              canonicalRepositoryRoot,
              testRoot))
          .toList();

      if (!sourceRoots.isEmpty()) {
        SpringMvcEndpointAnalysis endpointAnalysis = analyzer.analyze(repositoryRoot, sourceRoots);
        endpointAnalysis.endpoints().forEach(endpoint ->
            endpoints.add(new ModuleScopedEndpointFact(module.moduleId(), order, endpoint)));
        endpointEvidence.addAll(endpointAnalysis.evidence());

        SpringComponentAnalysis componentAnalysis = componentAnalyzer.analyze(repositoryRoot, sourceRoots);
        componentAnalysis.components().forEach(component ->
            components.add(new ModuleScopedComponentFact(module.moduleId(), order, component)));
        componentEvidence.addAll(componentAnalysis.evidence());
        componentAnalyzerRan = true;

        SpringRepositoryAnalysis springRepositoryAnalysis = springRepositoryAnalyzer.analyze(
            repositoryRoot,
            sourceRoots);
        springRepositoryAnalysis.repositories().forEach(repository ->
            springRepositories.add(new ModuleScopedSpringRepositoryFact(module.moduleId(), order, repository)));
        springRepositoryEvidence.addAll(springRepositoryAnalysis.evidence());
        springRepositoryAnalyzerRan = true;

        SpringConfigurationAnalysis springConfigurationAnalysis = springConfigurationAnalyzer.analyze(
            repositoryRoot,
            sourceRoots);
        springConfigurationAnalysis.configurationClasses().forEach(configuration ->
            springConfigurationClasses.add(new ModuleScopedSpringConfigurationClassFact(
                module.moduleId(),
                order,
                configuration)));
        springConfigurationAnalysis.configurationProperties().forEach(properties ->
            springConfigurationProperties.add(new ModuleScopedSpringConfigurationPropertiesFact(
                module.moduleId(),
                order,
                properties)));
        springConfigurationAnalysis.beanMethods().forEach(beanMethod ->
            springBeanMethods.add(new ModuleScopedSpringBeanMethodFact(
                module.moduleId(),
                order,
                beanMethod)));
        springConfigurationEvidence.addAll(springConfigurationAnalysis.evidence());
        springConfigurationAnalyzerRan = true;

        SpringBehaviorAnalysis springBehaviorAnalysis = springBehaviorAnalyzer.analyze(
            repositoryRoot,
            sourceRoots);
        springBehaviorAnalysis.transactionBoundaries().forEach(boundary ->
            springTransactionBoundaries.add(new ModuleScopedSpringTransactionBoundaryFact(
                module.moduleId(),
                order,
                boundary)));
        springBehaviorAnalysis.scheduledMethods().forEach(scheduledMethod ->
            springScheduledMethods.add(new ModuleScopedSpringScheduledMethodFact(
                module.moduleId(),
                order,
                scheduledMethod)));
        springBehaviorAnalysis.eventListeners().forEach(eventListener ->
            springEventListeners.add(new ModuleScopedSpringEventListenerFact(
                module.moduleId(),
                order,
                eventListener)));
        springBehaviorAnalysis.messagingListenerSignals().forEach(listener ->
            springMessagingListeners.add(new ModuleScopedSpringMessagingListenerFact(
                module.moduleId(),
                order,
                listener)));
        springBehaviorEvidence.addAll(springBehaviorAnalysis.evidence());
        springBehaviorAnalyzerRan = true;

        AnalysisWarningAnalysis securityWarningAnalysis = springSecurityConfigurationAnalyzer.analyze(
            repositoryRoot,
            sourceRoots,
            module.modulePath());
        securityWarningAnalysis.warnings().forEach(warning ->
            warnings.add(new ModuleScopedWarningFact(
                warning.id(),
                warning.category(),
                warning.signal(),
                module.moduleId(),
                order,
                warning.message(),
                warning.sourcePath(),
                warning.evidenceIds())));
        warningEvidence.addAll(securityWarningAnalysis.evidence());
        springSecurityAnalyzerRan = true;

        JpaEntityAnalysis entityAnalysis = entityAnalyzer.analyze(repositoryRoot, sourceRoots);
        entityAnalysis.entities().forEach(entity ->
            entities.add(new ModuleScopedEntityFact(module.moduleId(), order, entity)));
        entityAnalysis.embeddables().forEach(embeddable ->
            embeddables.add(new ModuleScopedEmbeddableFact(module.moduleId(), order, embeddable)));
        entityEvidence.addAll(entityAnalysis.evidence());
        entityAnalyzerRan = true;
      }

      if (!testRoots.isEmpty()) {
        TestInventoryAnalysis testAnalysis = testInventoryAnalyzer.analyze(
            repositoryRoot,
            sourceRoots,
            testRoots);
        if (!MODULE_ANALYSIS_NOT_DETECTED.equals(testAnalysis.analysisStatus())) {
          testAnalyzerRan = true;
        }
        testAnalysis.tests().forEach(test ->
            tests.add(new ModuleScopedTestFact(module.moduleId(), order, test)));
        testEvidence.addAll(testAnalysis.evidence());
      }

      List<String> excludedModulePaths = ".".equals(module.modulePath()) ? childModulePaths : List.of();
      AnalysisWarningAnalysis warningAnalysis = warningAnalyzer.analyzeModule(
          repositoryRoot,
          module.modulePath(),
          sourceRoots,
          excludedModulePaths);
      warningAnalyzerRan = true;
      warningAnalysis.warnings().forEach(warning ->
          warnings.add(new ModuleScopedWarningFact(
              warning.id(),
              warning.category(),
              warning.signal(),
              module.moduleId(),
              order,
              warning.message(),
              warning.sourcePath(),
              warning.evidenceIds())));
      warningEvidence.addAll(warningAnalysis.evidence());
    }

    List<ModuleScopedSpringRepositoryFact> springRepositoriesWithEntityRelations =
        inferRepositoryEntityRelations(springRepositories, entities);

    return new ModuleAwareScan(
        endpoints.stream().sorted(ENDPOINT_ORDER).toList(),
        warnings.stream().sorted(WARNING_ORDER).toList(),
        components.stream().sorted(COMPONENT_ORDER).toList(),
        springRepositoriesWithEntityRelations.stream().sorted(SPRING_REPOSITORY_ORDER).toList(),
        springConfigurationClasses.stream().sorted(SPRING_CONFIGURATION_CLASS_ORDER).toList(),
        springConfigurationProperties.stream().sorted(SPRING_CONFIGURATION_PROPERTIES_ORDER).toList(),
        springBeanMethods.stream().sorted(SPRING_BEAN_METHOD_ORDER).toList(),
        springTransactionBoundaries.stream().sorted(SPRING_TRANSACTION_BOUNDARY_ORDER).toList(),
        springScheduledMethods.stream().sorted(SPRING_SCHEDULED_METHOD_ORDER).toList(),
        springEventListeners.stream().sorted(SPRING_EVENT_LISTENER_ORDER).toList(),
        springMessagingListeners.stream().sorted(SPRING_MESSAGING_LISTENER_ORDER).toList(),
        entities.stream().sorted(ENTITY_ORDER).toList(),
        embeddables.stream().sorted(EMBEDDABLE_ORDER).toList(),
        tests.stream().sorted(TEST_CLASS_ORDER).toList(),
        warningAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        componentAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        springRepositoryAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        springConfigurationAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        springBehaviorAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        springBehaviorAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        springSecurityAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        entityAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        testAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        endpointEvidence,
        componentEvidence,
        springRepositoryEvidence,
        springConfigurationEvidence,
        springBehaviorEvidence,
        entityEvidence,
        testEvidence,
        warningEvidence);
  }

  private List<ModuleScopedSpringRepositoryFact> inferRepositoryEntityRelations(
      List<ModuleScopedSpringRepositoryFact> springRepositories,
      List<ModuleScopedEntityFact> entities) {
    Map<String, List<ModuleScopedEntityFact>> entitiesByClassName = new LinkedHashMap<>();
    for (ModuleScopedEntityFact entity : entities) {
      entitiesByClassName.computeIfAbsent(entity.fact().className(), ignored -> new ArrayList<>())
          .add(entity);
    }

    List<ModuleScopedSpringRepositoryFact> inferredRepositories = new ArrayList<>();
    for (ModuleScopedSpringRepositoryFact scopedRepository : springRepositories) {
      SpringRepositoryFact repository = scopedRepository.fact();
      if (!SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE.equals(
          repository.surfaceCategory())) {
        inferredRepositories.add(scopedRepository);
        continue;
      }

      SpringRepositoryFact updatedRepository = repositoryWithEntityRelation(
          repository,
          entitiesByClassName);
      inferredRepositories.add(new ModuleScopedSpringRepositoryFact(
          scopedRepository.moduleId(),
          scopedRepository.moduleOrder(),
          updatedRepository));
    }
    return inferredRepositories;
  }

  private SpringRepositoryFact repositoryWithEntityRelation(
      SpringRepositoryFact repository,
      Map<String, List<ModuleScopedEntityFact>> entitiesByClassName) {
    List<SpringRepositoryEntityGenericFact> entityGenericTypes = repository.entityGenericTypes();
    if (entityGenericTypes.isEmpty()
        || entityGenericTypes.stream().anyMatch(generic ->
            !SpringRepositoryAnalyzer.ENTITY_GENERIC_SUPPORTED.equals(generic.supportStatus()))) {
      return repository.withEntityRelation(
          SpringRepositoryAnalyzer.ENTITY_RELATION_UNSUPPORTED,
          null);
    }

    List<String> genericTypeNames = entityGenericTypes.stream()
        .map(SpringRepositoryEntityGenericFact::qualifiedTypeName)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (genericTypeNames.size() != 1) {
      return repository.withEntityRelation(
          SpringRepositoryAnalyzer.ENTITY_RELATION_AMBIGUOUS,
          null);
    }

    String genericTypeName = genericTypeNames.get(0);
    List<ModuleScopedEntityFact> matches = entitiesByClassName.getOrDefault(genericTypeName, List.of());
    if (matches.isEmpty()) {
      return repository.withEntityRelation(
          SpringRepositoryAnalyzer.ENTITY_RELATION_NOT_DETECTED,
          null);
    }
    if (matches.size() > 1) {
      return repository.withEntityRelation(
          SpringRepositoryAnalyzer.ENTITY_RELATION_AMBIGUOUS,
          null);
    }

    ModuleScopedEntityFact targetEntity = matches.get(0);
    List<String> relationEvidenceIds = new ArrayList<>();
    entityGenericTypes.stream()
        .flatMap(generic -> generic.evidenceIds().stream())
        .forEach(relationEvidenceIds::add);
    targetEntity.fact().evidenceIds().forEach(relationEvidenceIds::add);

    SpringRepositoryEntityRelationFact relation = new SpringRepositoryEntityRelationFact(
        SpringRepositoryAnalyzer.SUPPORT_TYPE_INFERRED,
        "repository_entity_generic",
        entityId(targetEntity.moduleId(), targetEntity.fact()),
        targetEntity.moduleId(),
        targetEntity.fact().className(),
        genericTypeName,
        "medium",
        null,
        relationEvidenceIds.stream().distinct().toList());
    return repository.withEntityRelation(
        SpringRepositoryAnalyzer.ENTITY_RELATION_INFERRED,
        relation);
  }

  private Map<String, Integer> moduleOrder(List<MavenModuleItem> modules) {
    Map<String, Integer> order = new LinkedHashMap<>();
    for (int index = 0; index < modules.size(); index++) {
      order.put(modules.get(index).moduleId(), index);
    }
    return order;
  }

  private List<ModuleScopedWarningFact> generatedSourceWarnings(
      MavenPluginAnalysis pluginAnalysis,
      Map<String, Integer> moduleOrder,
      List<MavenModuleItem> modules) {
    Map<String, MavenModuleItem> moduleById = moduleById(modules);
    List<ModuleScopedWarningFact> warnings = new ArrayList<>();
    for (MavenModulePlugins modulePlugins : pluginAnalysis.modules()) {
      MavenModuleItem module = moduleById.get(modulePlugins.moduleId());
      String modulePath = module == null ? modulePlugins.moduleId() : module.modulePath();
      String sourcePath = module == null ? "" : module.pomPath();
      int order = moduleOrder.getOrDefault(modulePlugins.moduleId(), Integer.MAX_VALUE);
      modulePlugins.plugins().forEach(plugin ->
          addGeneratedSourceWarnings(warnings, plugin, modulePlugins.moduleId(), modulePath, sourcePath, order));
      modulePlugins.pluginManagement().forEach(plugin ->
          addGeneratedSourceWarnings(warnings, plugin, modulePlugins.moduleId(), modulePath, sourcePath, order));
    }
    return warnings;
  }

  private void addGeneratedSourceWarnings(
      List<ModuleScopedWarningFact> warnings,
      MavenPluginDeclaration plugin,
      String moduleId,
      String modulePath,
      String sourcePath,
      int moduleOrder) {
    for (MavenPluginSignal signal : plugin.generatorSignals()) {
      if (PLUGIN_SIGNAL_OPENAPI_SWAGGER_CODEGEN.equals(signal.signal())) {
        warnings.add(generatedSourceWarning(
            WARNING_SIGNAL_MAVEN_OPENAPI_SWAGGER_CODEGEN_PLUGIN,
            moduleId,
            modulePath,
            sourcePath,
            moduleOrder,
            plugin,
            signal.evidenceIds(),
            "Maven OpenAPI/Swagger code generation plugin declaration detected; the analyzer "
                + "does not run code generation, scan generated sources by default, "
                + "or create endpoint/API facts from this build signal."));
      } else if (PLUGIN_SIGNAL_SOURCE_GENERATOR_PLUGIN.equals(signal.signal())) {
        warnings.add(generatedSourceWarning(
            WARNING_SIGNAL_MAVEN_GENERATOR_PLUGIN,
            moduleId,
            modulePath,
            sourcePath,
            moduleOrder,
            plugin,
            signal.evidenceIds(),
            "Maven source generator plugin declaration detected; the analyzer records the "
                + "source-visible build signal only and does not scan generated sources by default."));
      } else if (PLUGIN_SIGNAL_ANNOTATION_PROCESSOR.equals(signal.signal())) {
        warnings.add(generatedSourceWarning(
            WARNING_SIGNAL_MAVEN_ANNOTATION_PROCESSOR,
            moduleId,
            modulePath,
            sourcePath,
            moduleOrder,
            plugin,
            signal.evidenceIds(),
            "Maven annotation processor signal detected; the analyzer does not inspect "
                + "generated sources or infer generated APIs from processors."));
      }
    }

    for (MavenPluginSignal signal : plugin.configurationSignals()) {
      if (CONFIG_SIGNAL_GENERATED_SOURCES_CONFIG_PRESENT.equals(signal.signal())) {
        warnings.add(generatedSourceWarning(
            WARNING_SIGNAL_MAVEN_GENERATED_SOURCE_CONFIG,
            moduleId,
            modulePath,
            sourcePath,
            moduleOrder,
            plugin,
            signal.evidenceIds(),
            "Maven generated-source configuration signal detected; the analyzer records the "
                + "bounded build signal only and does not inspect configured generated output."));
      } else if (CONFIG_SIGNAL_ADD_SOURCE_GOAL_PRESENT.equals(signal.signal())) {
        warnings.add(generatedSourceWarning(
            WARNING_SIGNAL_MAVEN_BUILD_HELPER_ADD_SOURCE,
            moduleId,
            modulePath,
            sourcePath,
            moduleOrder,
            plugin,
            signal.evidenceIds(),
            "Maven build-helper add-source goal detected; the analyzer does not scan added "
                + "or generated sources by default."));
      }
    }
  }

  private ModuleScopedWarningFact generatedSourceWarning(
      String signal,
      String moduleId,
      String modulePath,
      String sourcePath,
      int moduleOrder,
      MavenPluginDeclaration plugin,
      List<String> evidenceIds,
      String message) {
    return new ModuleScopedWarningFact(
        generatedSourceWarningId(signal, modulePath, plugin),
        WARNING_CATEGORY_GENERATED_SOURCE,
        signal,
        moduleId,
        moduleOrder,
        message,
        sourcePath == null ? "" : sourcePath,
        evidenceIds);
  }

  private String generatedSourceWarningId(
      String signal,
      String modulePath,
      MavenPluginDeclaration plugin) {
    return "warning:"
        + WARNING_CATEGORY_GENERATED_SOURCE
        + ":"
        + signal
        + ":module:"
        + modulePath
        + ":"
        + plugin.declarationKind()
        + ":decl:"
        + ordinalText(plugin.declarationOrdinal());
  }

  private Optional<EvidenceRecord> buildFileEvidence(
      Path repositoryRoot,
      Path canonicalRepositoryRoot) throws IOException {
    Path buildFile = repositoryRoot.resolve(ROOT_BUILD_FILE);
    if (!ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, buildFile)) {
      return Optional.empty();
    }

    List<String> lines = Files.readAllLines(buildFile, StandardCharsets.UTF_8);
    Integer line = lines.isEmpty() ? null : 1;
    String lineRange = line == null ? "unknown" : line + "-" + line;
    String excerpt = lines.isEmpty() ? "" : EvidenceExcerpts.bounded(lines.get(0).trim());
    return Optional.of(new EvidenceRecord(
        "ev:" + ROOT_BUILD_FILE + ":" + lineRange + ":build_file:" + ROOT_BUILD_FILE,
        BUILD_FILE_SOURCE_TYPE,
        ROOT_BUILD_FILE,
        null,
        null,
        ROOT_BUILD_FILE,
        line,
        line,
        excerpt,
        HIGH_CONFIDENCE));
  }

  private List<String> detectedRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<String> candidates) {
    return candidates.stream()
        .filter(candidate -> ScanPathContainment.isDirectoryUnderRoot(
            canonicalRepositoryRoot,
            repositoryRoot.resolve(candidate)))
        .sorted()
        .toList();
  }

  private String endpointsMarkdown(
      ProjectModules modules,
      List<ModuleScopedEndpointFact> endpoints,
      List<OpenApiOperationFact> openApiOperations,
      List<ModuleScopedWarningFact> warnings) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("# Endpoints\n\n");

    markdown.append("## Source-Visible Spring MVC Endpoints\n\n");
    if (endpoints.isEmpty()) {
      markdown.append("No Spring MVC endpoints detected in supported module source roots.\n\n");
    }

    Map<String, MavenModuleItem> moduleById = moduleById(modules.items());
    appendEndpointCategoryMarkdown(
        markdown,
        moduleById,
        endpoints,
        API_SURFACE_CATEGORY_SOURCE_VISIBLE,
        "Direct Handler Mappings",
        "No source-visible direct handler mappings detected.");
    appendEndpointCategoryMarkdown(
        markdown,
        moduleById,
        endpoints,
        API_SURFACE_CATEGORY_INTERFACE_DECLARED,
        "Source-Visible Interface-Declared Mappings",
        "No source-visible interface-declared mappings detected.");
    appendDeclaredOpenApiOperations(markdown, moduleById, openApiOperations);
    appendApiWarningSections(markdown, moduleById, warnings);

    return withoutTrailingBlankLine(markdown);
  }

  private void appendEndpointCategoryMarkdown(
      StringBuilder markdown,
      Map<String, MavenModuleItem> moduleById,
      List<ModuleScopedEndpointFact> endpoints,
      String apiSurfaceCategory,
      String heading,
      String emptyMessage) {
    markdown.append("### ").append(heading).append("\n\n");
    List<ModuleScopedEndpointFact> categoryEndpoints = endpoints.stream()
        .filter(endpoint -> apiSurfaceCategory.equals(apiSurfaceCategory(endpoint.fact().mappingSource())))
        .toList();
    if (categoryEndpoints.isEmpty()) {
      markdown.append(emptyMessage).append("\n\n");
      return;
    }

    String currentModuleId = null;
    for (EndpointRow row : endpointRows(categoryEndpoints)) {
      if (!row.moduleId().equals(currentModuleId)) {
        currentModuleId = row.moduleId();
        appendEndpointModuleHeading(markdown, "####", currentModuleId, moduleById.get(currentModuleId));
      }
      SpringMvcEndpointFact endpoint = row.endpoint();
      markdown.append("##### ")
          .append(MarkdownRenderer.text(row.methodLabel() + " " + row.path()))
          .append("\n\n");
      markdown.append("- Module: ")
          .append(moduleLabel(row.moduleId(), moduleById.get(row.moduleId())))
          .append("\n");
      markdown.append("- API surface category: ")
          .append(code(apiSurfaceCategory(endpoint.mappingSource())))
          .append("\n");
      markdown.append("- Controller: ").append(code(endpoint.controllerClass())).append("\n");
      markdown.append("- Handler: ").append(code(endpoint.handlerMethod())).append("\n");
      markdown.append("- Mapping source: ")
          .append(mappingSourceLabel(endpoint.mappingSource()))
          .append("\n");
      markdown.append("- HTTP methods: ").append(httpMethods(endpoint)).append("\n");
      markdown.append("- Request parameters: ")
          .append(requestParameters(endpoint.requestParameters()))
          .append("\n");
      markdown.append("- Request body: ")
          .append(nullableCode(endpoint.requestBodyType()))
          .append("\n");
      markdown.append("- Response: ")
          .append(nullableCode(endpoint.declaredResponseType()))
          .append("\n");
      markdown.append("- Evidence: ").append(codeList(endpoint.evidenceIds())).append("\n\n");
    }
  }

  private void appendDeclaredOpenApiOperations(
      StringBuilder markdown,
      Map<String, MavenModuleItem> moduleById,
      List<OpenApiOperationFact> openApiOperations) {
    markdown.append("## Declared OpenAPI Operations\n\n");
    if (openApiOperations.isEmpty()) {
      markdown.append("No spec-backed declared OpenAPI operations recorded.\n\n");
      return;
    }

    String currentModuleId = null;
    for (OpenApiOperationFact operation : openApiOperations) {
      String moduleId = operation.moduleId();
      if (!Objects.equals(moduleId, currentModuleId)) {
        currentModuleId = moduleId;
        markdown.append("### Module ")
            .append(moduleLabel(moduleId, moduleById.get(moduleId)))
            .append("\n\n");
      }
      markdown.append("#### Declared ")
          .append(code(operation.httpMethod() + " " + operation.path()))
          .append("\n\n");
      markdown.append("- API surface category: ")
          .append(code(operation.apiSurfaceCategory()))
          .append("\n");
      markdown.append("- Module: ")
          .append(moduleLabel(moduleId, moduleById.get(moduleId)))
          .append("\n");
      markdown.append("- Spec path: ")
          .append(code(operation.specPath()))
          .append("\n");
      markdown.append("- HTTP method: ")
          .append(code(operation.httpMethod()))
          .append("\n");
      markdown.append("- Declared path: ")
          .append(code(operation.path()))
          .append("\n");
      markdown.append("- Operation ID: ")
          .append(nullableCode(operation.operationId()))
          .append("\n");
      markdown.append("- Tags: ")
          .append(codeList(operation.tags()))
          .append("\n");
      markdown.append("- Implementation status: ")
          .append(code(operation.implementationStatus()))
          .append("\n");
      markdown.append("- Evidence: ")
          .append(codeList(operation.evidenceIds()))
          .append("\n\n");
    }
  }

  private void appendApiWarningSections(
      StringBuilder markdown,
      Map<String, MavenModuleItem> moduleById,
      List<ModuleScopedWarningFact> warnings) {
    markdown.append("## Generated And Hidden API Warnings\n\n");
    appendApiWarningSection(
        markdown,
        moduleById,
        warningsForIds(warnings, generatedSourceApiWarningIds(warnings)),
        "Generated-Source API Signals",
        "No generated-source API warning signals recorded.");
    appendApiWarningSection(
        markdown,
        moduleById,
        warningsForIds(warnings, repositoryRestWarningIds(warnings)),
        "Repository REST Warnings",
        "No repository-rest warnings recorded.");
    appendApiWarningSection(
        markdown,
        moduleById,
        warningsForIds(warnings, hiddenHttpWarningIds(warnings)),
        "Hidden HTTP Warnings",
        "No hidden HTTP warnings recorded.");
  }

  private void appendApiWarningSection(
      StringBuilder markdown,
      Map<String, MavenModuleItem> moduleById,
      List<ModuleScopedWarningFact> warnings,
      String heading,
      String emptyMessage) {
    markdown.append("### ").append(heading).append("\n\n");
    if (warnings.isEmpty()) {
      markdown.append(emptyMessage).append("\n\n");
      return;
    }

    for (ModuleScopedWarningFact warning : warnings) {
      markdown.append("#### Warning ")
          .append(code(warning.category() + ":" + warning.signal()))
          .append("\n\n");
      markdown.append("- Module: ")
          .append(moduleLabel(warning.moduleId(), moduleById.get(warning.moduleId())))
          .append("\n");
      markdown.append("- Warning category: ")
          .append(code(warning.category()))
          .append("\n");
      markdown.append("- Signal: ")
          .append(code(warning.signal()))
          .append("\n");
      markdown.append("- Source path: ")
          .append(nullableCode(warning.sourcePath()))
          .append("\n");
      markdown.append("- Message: ")
          .append(MarkdownRenderer.text(warning.message()))
          .append("\n");
      markdown.append("- Evidence: ")
          .append(codeList(warning.evidenceIds()))
          .append("\n\n");
    }
  }

  private List<ModuleScopedWarningFact> warningsForIds(
      List<ModuleScopedWarningFact> warnings,
      List<String> warningIds) {
    Map<String, ModuleScopedWarningFact> warningById = new LinkedHashMap<>();
    for (ModuleScopedWarningFact warning : warnings) {
      warningById.put(warning.id(), warning);
    }

    List<ModuleScopedWarningFact> selected = new ArrayList<>();
    for (String warningId : warningIds) {
      ModuleScopedWarningFact warning = warningById.get(warningId);
      if (warning != null) {
        selected.add(warning);
      }
    }
    return selected;
  }

  private String withoutTrailingBlankLine(StringBuilder markdown) {
    if (markdown.length() >= 2
        && markdown.charAt(markdown.length() - 1) == '\n'
        && markdown.charAt(markdown.length() - 2) == '\n') {
      markdown.deleteCharAt(markdown.length() - 1);
    }
    return markdown.toString();
  }

  private Map<String, MavenModuleItem> moduleById(List<MavenModuleItem> modules) {
    Map<String, MavenModuleItem> moduleById = new LinkedHashMap<>();
    for (MavenModuleItem module : modules) {
      moduleById.put(module.moduleId(), module);
    }
    return moduleById;
  }

  private void appendEndpointModuleHeading(
      StringBuilder markdown,
      String headingPrefix,
      String moduleId,
      MavenModuleItem module) {
    markdown.append(headingPrefix)
        .append(" Module ")
        .append(moduleLabel(moduleId, module))
        .append("\n\n");
    if (module == null) {
      markdown.append("- Module metadata: not recorded in `project.modules.items`.\n\n");
      return;
    }
    markdown.append("- Module path: ").append(code(module.modulePath())).append("\n");
    markdown.append("- Support status: ").append(code(module.supportStatus())).append("\n\n");
  }

  private String projectMapJson(
      ProjectLayout layout,
      ModuleAwareScan scan,
      MavenMetadataAnalysis metadataAnalysis,
      MavenDependencyAnalysis dependencyAnalysis,
      MavenPluginAnalysis pluginAnalysis,
      ResourceConfigAnalysis resourceConfigAnalysis,
      SpringBootApplicationAnalysis springBootApplicationAnalysis,
      OpenApiSpecDiscoveryAnalysis openApiSpecDiscoveryAnalysis,
      OpenApiOperationAnalysis openApiOperationAnalysis,
      DocumentDiscoveryAnalysis documentDiscoveryAnalysis) {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    appendIndentedStringField(json, 1, "schema_version", SCHEMA_VERSION, true);
    json.append("  \"project\": {\n");
    appendIndentedStringField(json, 2, "root", ".", true);
    json.append("    \"build\": {\n");
    appendIndentedStringField(json, 3, "system", layout.build().system(), true);
    appendIndentedNullableStringField(
        json,
        3,
        "root_build_file",
        layout.build().rootBuildFile(),
        true);
    appendIndentedStringArrayField(json, 3, "evidence_ids", layout.build().evidenceIds(), false);
    json.append("    },\n");
    appendIndentedStringArrayField(json, 2, "source_roots", layout.sourceRoots(), true);
    appendIndentedStringArrayField(json, 2, "test_roots", layout.testRoots(), true);
    appendModules(
        json,
        layout.modules(),
        metadataByModuleId(metadataAnalysis),
        dependenciesByModuleId(dependencyAnalysis),
        pluginsByModuleId(pluginAnalysis),
        resourceConfigByModuleId(resourceConfigAnalysis),
        springBootApplicationsByModuleId(springBootApplicationAnalysis));
    json.append("  },\n");
    json.append("  \"endpoints\": [");
    if (scan.endpoints().isEmpty()) {
      json.append("],\n");
    } else {
      json.append("\n");
      for (int index = 0; index < scan.endpoints().size(); index++) {
        appendEndpoint(json, scan.endpoints().get(index), index < scan.endpoints().size() - 1);
      }
      json.append("  ],\n");
    }
    appendApiSurface(json, scan, openApiSpecDiscoveryAnalysis, openApiOperationAnalysis);
    appendSpringApplicationSurface(json, scan);
    json.append("  \"warnings\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.warningAnalysisStatus(), true);
    appendWarnings(json, scan.warnings());
    json.append("  },\n");
    json.append("  \"components\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.componentAnalysisStatus(), true);
    appendComponents(json, scan.components());
    json.append("  },\n");
    json.append("  \"entities\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.entityAnalysisStatus(), true);
    appendEntities(json, scan.entities());
    appendEmbeddables(json, scan.embeddables(), scan.entityAnalysisStatus());
    json.append("  },\n");
    json.append("  \"tests\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.testAnalysisStatus(), true);
    appendTests(json, scan.tests());
    json.append("  },\n");
    appendDocuments(json, documentDiscoveryAnalysis, true);
    appendQuality(json, qualitySignals(scan));
    json.append("}\n");
    return json.toString();
  }

  private void appendDocuments(
      StringBuilder json,
      DocumentDiscoveryAnalysis documentDiscoveryAnalysis,
      boolean trailingComma) {
    json.append("  \"documents\": {\n");
    appendIndentedStringField(
        json,
        2,
        "analysis_status",
        documentDiscoveryAnalysis.analysisStatus(),
        true);
    appendDocumentDiscovery(json, documentDiscoveryAnalysis.discoveryPolicy(), true);
    appendDocumentItems(json, documentDiscoveryAnalysis.documents(), false);
    json.append("  }");
    appendLineEnding(json, trailingComma);
  }

  private void appendDocumentDiscovery(
      StringBuilder json,
      DocumentDiscoveryPolicy discoveryPolicy,
      boolean trailingComma) {
    json.append("    \"discovery\": {\n");
    appendIndentedStringField(json, 3, "scope", discoveryPolicy.scope(), true);
    appendIndentedStringField(json, 3, "path_policy", discoveryPolicy.pathPolicy(), true);
    appendIndentedStringField(json, 3, "symlink_policy", discoveryPolicy.symlinkPolicy(), true);
    appendIndentedStringArrayField(json, 3, "included_patterns", discoveryPolicy.includedPatterns(), true);
    appendIndentedStringArrayField(json, 3, "excluded_patterns", discoveryPolicy.excludedPatterns(), false);
    json.append("    }");
    appendLineEnding(json, trailingComma);
  }

  private void appendDocumentItems(StringBuilder json, List<DocumentFileFact> documents, boolean trailingComma) {
    json.append("    \"items\": [");
    if (documents.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < documents.size(); index++) {
      appendDocument(json, documents.get(index), index < documents.size() - 1);
    }
    json.append("    ]");
    appendLineEnding(json, trailingComma);
  }

  private void appendDocument(StringBuilder json, DocumentFileFact document, boolean trailingComma) {
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", document.id(), true);
    appendIndentedStringField(json, 4, "document_kind", document.documentKind(), true);
    appendIndentedStringField(json, 4, "format", document.format(), true);
    appendIndentedNullableStringField(json, 4, "module_id", document.moduleId(), true);
    appendIndentedStringField(json, 4, "path", document.path(), true);
    appendIndentedStringField(json, 4, "title", document.title(), true);
    appendIndentedStringField(json, 4, "title_source", document.titleSource(), true);
    appendIndentedStringField(json, 4, "discovery_source", document.discoverySource(), true);
    appendIndentedStringArrayField(json, 4, "headings", document.headings(), true);
    appendIndentedStringArrayField(json, 4, "chunks", document.chunks(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", document.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private Map<String, MavenModuleMetadata> metadataByModuleId(MavenMetadataAnalysis metadataAnalysis) {
    Map<String, MavenModuleMetadata> metadataByModuleId = new LinkedHashMap<>();
    for (MavenModuleMetadata metadata : metadataAnalysis.modules()) {
      metadataByModuleId.put(metadata.moduleId(), metadata);
    }
    return metadataByModuleId;
  }

  private Map<String, MavenModuleDependencies> dependenciesByModuleId(
      MavenDependencyAnalysis dependencyAnalysis) {
    Map<String, MavenModuleDependencies> dependenciesByModuleId = new LinkedHashMap<>();
    for (MavenModuleDependencies dependencies : dependencyAnalysis.modules()) {
      dependenciesByModuleId.put(dependencies.moduleId(), dependencies);
    }
    return dependenciesByModuleId;
  }

  private Map<String, MavenModulePlugins> pluginsByModuleId(
      MavenPluginAnalysis pluginAnalysis) {
    Map<String, MavenModulePlugins> pluginsByModuleId = new LinkedHashMap<>();
    for (MavenModulePlugins plugins : pluginAnalysis.modules()) {
      pluginsByModuleId.put(plugins.moduleId(), plugins);
    }
    return pluginsByModuleId;
  }

  private Map<String, ModuleResourceConfig> resourceConfigByModuleId(
      ResourceConfigAnalysis resourceConfigAnalysis) {
    Map<String, ModuleResourceConfig> resourceConfigByModuleId = new LinkedHashMap<>();
    for (ModuleResourceConfig resourceConfig : resourceConfigAnalysis.modules()) {
      resourceConfigByModuleId.put(resourceConfig.moduleId(), resourceConfig);
    }
    return resourceConfigByModuleId;
  }

  private Map<String, ModuleSpringBootApplications> springBootApplicationsByModuleId(
      SpringBootApplicationAnalysis springBootApplicationAnalysis) {
    Map<String, ModuleSpringBootApplications> applicationsByModuleId = new LinkedHashMap<>();
    for (ModuleSpringBootApplications applications : springBootApplicationAnalysis.modules()) {
      applicationsByModuleId.put(applications.moduleId(), applications);
    }
    return applicationsByModuleId;
  }

  private void appendModules(
      StringBuilder json,
      ProjectModules modules,
      Map<String, MavenModuleMetadata> metadataByModuleId,
      Map<String, MavenModuleDependencies> dependenciesByModuleId,
      Map<String, MavenModulePlugins> pluginsByModuleId,
      Map<String, ModuleResourceConfig> resourceConfigByModuleId,
      Map<String, ModuleSpringBootApplications> springBootApplicationsByModuleId) {
    json.append("    \"modules\": {\n");
    appendIndentedStringField(json, 3, "analysis_status", modules.analysisStatus(), true);
    json.append("      \"items\": [");
    if (modules.items().isEmpty()) {
      json.append("]\n");
      json.append("    }\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < modules.items().size(); index++) {
      MavenModuleItem module = modules.items().get(index);
      json.append("        {\n");
      appendIndentedStringField(json, 5, "module_id", module.moduleId(), true);
      appendIndentedStringField(json, 5, "module_path", module.modulePath(), true);
      appendIndentedNullableStringField(json, 5, "pom_path", module.pomPath(), true);
      appendIndentedStringArrayField(json, 5, "source_roots", module.sourceRoots(), true);
      appendIndentedStringArrayField(json, 5, "test_roots", module.testRoots(), true);
      appendIndentedStringField(json, 5, "support_status", module.supportStatus(), true);
      appendIndentedStringField(json, 5, "declaration_kind", module.declarationKind(), true);
      appendIndentedStringField(json, 5, "declared_path", module.declaredPath(), true);
      appendIndentedStringArrayField(
          json,
          5,
          "declaration_evidence_ids",
          module.declarationEvidenceIds(),
          true);
      appendIndentedStringArrayField(json, 5, "pom_evidence_ids", module.pomEvidenceIds(), true);
      appendBuildConfig(
          json,
          metadataForModule(module, metadataByModuleId),
          dependenciesForModule(module, dependenciesByModuleId),
          pluginsForModule(module, pluginsByModuleId),
          resourceConfigForModule(module, resourceConfigByModuleId),
          springBootApplicationsForModule(module, springBootApplicationsByModuleId),
          false);
      json.append("        }");
      if (index < modules.items().size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("      ]\n");
    json.append("    }\n");
  }

  private MavenModuleMetadata metadataForModule(
      MavenModuleItem module,
      Map<String, MavenModuleMetadata> metadataByModuleId) {
    MavenModuleMetadata metadata = metadataByModuleId.get(module.moduleId());
    if (metadata != null) {
      return metadata;
    }
    return notDetectedMetadata(module.moduleId());
  }

  private MavenModuleMetadata notDetectedMetadata(String moduleId) {
    MavenMetadataParent parent = new MavenMetadataParent(
        ANALYSIS_NOT_DETECTED,
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared());
    return new MavenModuleMetadata(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        parent);
  }

  private MavenModuleDependencies dependenciesForModule(
      MavenModuleItem module,
      Map<String, MavenModuleDependencies> dependenciesByModuleId) {
    MavenModuleDependencies dependencies = dependenciesByModuleId.get(module.moduleId());
    if (dependencies != null) {
      return dependencies;
    }
    return notDetectedDependencies(module.moduleId());
  }

  private MavenModuleDependencies notDetectedDependencies(String moduleId) {
    return new MavenModuleDependencies(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private MavenModulePlugins pluginsForModule(
      MavenModuleItem module,
      Map<String, MavenModulePlugins> pluginsByModuleId) {
    MavenModulePlugins plugins = pluginsByModuleId.get(module.moduleId());
    if (plugins != null) {
      return plugins;
    }
    return notDetectedPlugins(module.moduleId());
  }

  private MavenModulePlugins notDetectedPlugins(String moduleId) {
    return new MavenModulePlugins(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private ModuleResourceConfig resourceConfigForModule(
      MavenModuleItem module,
      Map<String, ModuleResourceConfig> resourceConfigByModuleId) {
    ModuleResourceConfig resourceConfig = resourceConfigByModuleId.get(module.moduleId());
    if (resourceConfig != null) {
      return resourceConfig;
    }
    return notDetectedResourceConfig(module.moduleId());
  }

  private ModuleResourceConfig notDetectedResourceConfig(String moduleId) {
    return new ModuleResourceConfig(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        ANALYSIS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private ModuleSpringBootApplications springBootApplicationsForModule(
      MavenModuleItem module,
      Map<String, ModuleSpringBootApplications> springBootApplicationsByModuleId) {
    ModuleSpringBootApplications applications = springBootApplicationsByModuleId.get(module.moduleId());
    if (applications != null) {
      return applications;
    }
    return notDetectedSpringBootApplications(module.moduleId());
  }

  private ModuleSpringBootApplications notDetectedSpringBootApplications(String moduleId) {
    return new ModuleSpringBootApplications(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        List.of());
  }

  private void appendBuildConfig(
      StringBuilder json,
      MavenModuleMetadata metadata,
      MavenModuleDependencies dependencies,
      MavenModulePlugins plugins,
      ModuleResourceConfig resourceConfig,
      ModuleSpringBootApplications springBootApplications,
      boolean trailingComma) {
    indent(json, 5);
    json.append("\"build_config\": {\n");
    appendIndentedStringField(
        json,
        6,
        "analysis_status",
        buildConfigAnalysisStatus(metadata, dependencies, plugins, resourceConfig, springBootApplications),
        true);
    appendMavenBuildConfig(json, metadata, dependencies, plugins);
    appendResourceRootSection(json, resourceConfig, true);
    appendConfigFileSection(json, resourceConfig, true);
    appendSpringBootApplicationSection(json, springBootApplications, false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private String buildConfigAnalysisStatus(
      MavenModuleMetadata metadata,
      MavenModuleDependencies dependencies,
      MavenModulePlugins plugins,
      ModuleResourceConfig resourceConfig,
      ModuleSpringBootApplications springBootApplications) {
    if (ANALYSIS_ANALYZED.equals(metadata.analysisStatus())
        || ANALYSIS_ANALYZED.equals(dependencies.analysisStatus())
        || ANALYSIS_ANALYZED.equals(plugins.analysisStatus())
        || ANALYSIS_ANALYZED.equals(resourceConfig.resourceAnalysisStatus())
        || ANALYSIS_ANALYZED.equals(resourceConfig.configFileAnalysisStatus())
        || ANALYSIS_ANALYZED.equals(springBootApplications.analysisStatus())) {
      return ANALYSIS_ANALYZED;
    }
    return ANALYSIS_NOT_DETECTED;
  }

  private void appendMavenBuildConfig(
      StringBuilder json,
      MavenModuleMetadata metadata,
      MavenModuleDependencies dependencies,
      MavenModulePlugins plugins) {
    indent(json, 6);
    json.append("\"maven\": {\n");
    appendMavenMetadata(json, metadata);
    appendMavenDependencySection(
        json,
        7,
        "dependencies",
        dependencies.analysisStatus(),
        dependencies.dependencies(),
        true);
    appendMavenDependencySection(
        json,
        7,
        "dependency_management",
        dependencies.analysisStatus(),
        dependencies.dependencyManagement(),
        true);
    appendMavenPluginSection(
        json,
        7,
        "plugins",
        plugins.analysisStatus(),
        plugins.plugins(),
        true);
    appendMavenPluginSection(
        json,
        7,
        "plugin_management",
        plugins.analysisStatus(),
        plugins.pluginManagement(),
        false);
    indent(json, 6);
    json.append("},\n");
  }

  private void appendMavenMetadata(StringBuilder json, MavenModuleMetadata metadata) {
    indent(json, 7);
    json.append("\"metadata\": {\n");
    appendIndentedStringField(json, 8, "analysis_status", metadata.analysisStatus(), true);
    appendMavenValue(json, 8, "group_id", metadata.groupId(), true);
    appendMavenValue(json, 8, "artifact_id", metadata.artifactId(), true);
    appendMavenValue(json, 8, "version", metadata.version(), true);
    appendMavenValue(json, 8, "packaging", metadata.packaging(), true);
    appendMavenParent(json, metadata.parent(), false);
    indent(json, 7);
    json.append("},\n");
  }

  private void appendMavenParent(
      StringBuilder json,
      MavenMetadataParent parent,
      boolean trailingComma) {
    indent(json, 8);
    json.append("\"parent\": {\n");
    appendIndentedStringField(json, 9, "analysis_status", parent.analysisStatus(), true);
    appendMavenValue(json, 9, "group_id", parent.groupId(), true);
    appendMavenValue(json, 9, "artifact_id", parent.artifactId(), true);
    appendMavenValue(json, 9, "version", parent.version(), true);
    appendMavenValue(json, 9, "relative_path", parent.relativePath(), false);
    indent(json, 8);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenValue(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      MavenMetadataValue value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "value", value.value(), true);
    appendIndentedStringField(json, indentLevel + 1, "value_kind", value.valueKind(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", value.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenDependencySection(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      String analysisStatus,
      List<MavenDependencyDeclaration> dependencies,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedStringField(json, indentLevel + 1, "analysis_status", analysisStatus, true);
    indent(json, indentLevel + 1);
    json.append("\"items\": [");
    if (dependencies.isEmpty()) {
      json.append("]\n");
      indent(json, indentLevel);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < dependencies.size(); index++) {
      appendMavenDependency(
          json,
          indentLevel + 2,
          dependencies.get(index),
          index < dependencies.size() - 1);
    }
    indent(json, indentLevel + 1);
    json.append("]\n");
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenDependency(
      StringBuilder json,
      int indentLevel,
      MavenDependencyDeclaration dependency,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("{\n");
    appendIndentedStringField(json, indentLevel + 1, "id", dependency.id(), true);
    appendIndentedStringField(
        json,
        indentLevel + 1,
        "declaration_kind",
        dependency.declarationKind(),
        true);
    appendIndentedIntegerField(
        json,
        indentLevel + 1,
        "declaration_ordinal",
        dependency.declarationOrdinal(),
        true);
    appendMavenValue(json, indentLevel + 1, "group_id", dependency.groupId(), true);
    appendMavenValue(json, indentLevel + 1, "artifact_id", dependency.artifactId(), true);
    appendMavenValue(json, indentLevel + 1, "version", dependency.version(), true);
    appendMavenValue(json, indentLevel + 1, "scope", dependency.scope(), true);
    appendMavenValue(json, indentLevel + 1, "optional", dependency.optional(), true);
    appendMavenValue(json, indentLevel + 1, "type", dependency.type(), true);
    appendMavenValue(json, indentLevel + 1, "classifier", dependency.classifier(), true);
    appendIndentedStringArrayField(
        json,
        indentLevel + 1,
        "evidence_ids",
        dependency.evidenceIds(),
        false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenPluginSection(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      String analysisStatus,
      List<MavenPluginDeclaration> plugins,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedStringField(json, indentLevel + 1, "analysis_status", analysisStatus, true);
    indent(json, indentLevel + 1);
    json.append("\"items\": [");
    if (plugins.isEmpty()) {
      json.append("]\n");
      indent(json, indentLevel);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < plugins.size(); index++) {
      appendMavenPlugin(
          json,
          indentLevel + 2,
          plugins.get(index),
          index < plugins.size() - 1);
    }
    indent(json, indentLevel + 1);
    json.append("]\n");
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenPlugin(
      StringBuilder json,
      int indentLevel,
      MavenPluginDeclaration plugin,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("{\n");
    appendIndentedStringField(json, indentLevel + 1, "id", plugin.id(), true);
    appendIndentedStringField(
        json,
        indentLevel + 1,
        "declaration_kind",
        plugin.declarationKind(),
        true);
    appendIndentedIntegerField(
        json,
        indentLevel + 1,
        "declaration_ordinal",
        plugin.declarationOrdinal(),
        true);
    appendMavenValue(json, indentLevel + 1, "group_id", plugin.groupId(), true);
    appendMavenValue(json, indentLevel + 1, "artifact_id", plugin.artifactId(), true);
    appendMavenValue(json, indentLevel + 1, "version", plugin.version(), true);
    appendMavenPluginExecutions(json, indentLevel + 1, plugin.executions());
    appendMavenPluginSignals(json, indentLevel + 1, "configuration_signals", plugin.configurationSignals(), true);
    appendMavenPluginSignals(json, indentLevel + 1, "generator_signals", plugin.generatorSignals(), true);
    appendIndentedStringArrayField(
        json,
        indentLevel + 1,
        "evidence_ids",
        plugin.evidenceIds(),
        false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenPluginExecutions(
      StringBuilder json,
      int indentLevel,
      List<MavenPluginExecution> executions) {
    indent(json, indentLevel);
    json.append("\"executions\": [");
    if (executions.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < executions.size(); index++) {
      appendMavenPluginExecution(
          json,
          indentLevel + 1,
          executions.get(index),
          index < executions.size() - 1);
    }
    indent(json, indentLevel);
    json.append("],\n");
  }

  private void appendMavenPluginExecution(
      StringBuilder json,
      int indentLevel,
      MavenPluginExecution execution,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "execution_id", execution.executionId(), true);
    appendMavenValue(json, indentLevel + 1, "phase", execution.phase(), true);
    appendMavenValueArray(json, indentLevel + 1, "goals", execution.goals(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", execution.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenValueArray(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      List<MavenMetadataValue> values,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": [");
    if (values.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < values.size(); index++) {
      appendMavenValueObject(
          json,
          indentLevel + 1,
          values.get(index),
          index < values.size() - 1);
    }
    indent(json, indentLevel);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenValueObject(
      StringBuilder json,
      int indentLevel,
      MavenMetadataValue value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "value", value.value(), true);
    appendIndentedStringField(json, indentLevel + 1, "value_kind", value.valueKind(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", value.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenPluginSignals(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      List<MavenPluginSignal> signals,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": [");
    if (signals.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < signals.size(); index++) {
      MavenPluginSignal signal = signals.get(index);
      indent(json, indentLevel + 1);
      json.append("{\n");
      appendIndentedStringField(json, indentLevel + 2, "signal", signal.signal(), true);
      appendIndentedStringArrayField(json, indentLevel + 2, "evidence_ids", signal.evidenceIds(), false);
      indent(json, indentLevel + 1);
      json.append("}");
      if (index < signals.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, indentLevel);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendResourceRootSection(
      StringBuilder json,
      ModuleResourceConfig resourceConfig,
      boolean trailingComma) {
    indent(json, 6);
    json.append("\"resources\": {\n");
    appendIndentedStringField(json, 7, "analysis_status", resourceConfig.resourceAnalysisStatus(), true);
    indent(json, 7);
    json.append("\"items\": [");
    List<ResourceRootFact> resourceRoots = resourceConfig.resourceRoots().stream()
        .sorted(RESOURCE_ROOT_ORDER)
        .toList();
    if (resourceRoots.isEmpty()) {
      json.append("]\n");
      indent(json, 6);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < resourceRoots.size(); index++) {
      ResourceRootFact resourceRoot = resourceRoots.get(index);
      indent(json, 8);
      json.append("{\n");
      appendIndentedStringField(json, 9, "id", resourceRoot.id(), true);
      appendIndentedStringField(json, 9, "scope", resourceRoot.scope(), true);
      appendIndentedStringField(json, 9, "path", resourceRoot.path(), true);
      appendIndentedStringArrayField(json, 9, "evidence_ids", resourceRoot.evidenceIds(), false);
      indent(json, 8);
      json.append("}");
      if (index < resourceRoots.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, 7);
    json.append("]\n");
    indent(json, 6);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendConfigFileSection(
      StringBuilder json,
      ModuleResourceConfig resourceConfig,
      boolean trailingComma) {
    indent(json, 6);
    json.append("\"config_files\": {\n");
    appendIndentedStringField(json, 7, "analysis_status", resourceConfig.configFileAnalysisStatus(), true);
    indent(json, 7);
    json.append("\"items\": [");
    List<ConfigFileFact> configFiles = resourceConfig.configFiles().stream()
        .sorted(CONFIG_FILE_ORDER)
        .toList();
    if (configFiles.isEmpty()) {
      json.append("]\n");
      indent(json, 6);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < configFiles.size(); index++) {
      ConfigFileFact configFile = configFiles.get(index);
      indent(json, 8);
      json.append("{\n");
      appendIndentedStringField(json, 9, "id", configFile.id(), true);
      appendIndentedStringField(json, 9, "path", configFile.path(), true);
      appendIndentedStringField(json, 9, "resource_scope", configFile.resourceScope(), true);
      appendIndentedStringField(json, 9, "config_kind", configFile.configKind(), true);
      appendIndentedStringField(json, 9, "format", configFile.format(), true);
      appendIndentedNullableStringField(json, 9, "profile_name", configFile.profileName(), true);
      appendIndentedNullableStringField(json, 9, "profile_source", configFile.profileSource(), true);
      appendIndentedStringArrayField(json, 9, "evidence_ids", configFile.evidenceIds(), false);
      indent(json, 8);
      json.append("}");
      if (index < configFiles.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, 7);
    json.append("]\n");
    indent(json, 6);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringBootApplicationSection(
      StringBuilder json,
      ModuleSpringBootApplications applications,
      boolean trailingComma) {
    indent(json, 6);
    json.append("\"spring_boot_applications\": {\n");
    appendIndentedStringField(json, 7, "analysis_status", applications.analysisStatus(), true);
    indent(json, 7);
    json.append("\"items\": [");
    List<SpringBootApplicationFact> sortedApplications = applications.applications().stream()
        .sorted(SPRING_BOOT_APPLICATION_ORDER)
        .toList();
    if (sortedApplications.isEmpty()) {
      json.append("]\n");
      indent(json, 6);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedApplications.size(); index++) {
      appendSpringBootApplication(
          json,
          sortedApplications.get(index),
          index < sortedApplications.size() - 1);
    }
    indent(json, 7);
    json.append("]\n");
    indent(json, 6);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringBootApplication(
      StringBuilder json,
      SpringBootApplicationFact application,
      boolean trailingComma) {
    indent(json, 8);
    json.append("{\n");
    appendIndentedStringField(json, 9, "id", application.id(), true);
    appendIndentedStringField(json, 9, "class_name", application.className(), true);
    appendIndentedStringField(json, 9, "source_path", application.sourcePath(), true);
    appendIndentedStringField(json, 9, "application_signal", application.applicationSignal(), true);
    indent(json, 9);
    json.append("\"main_method\": {\n");
    appendIndentedBooleanField(json, 10, "present", application.mainMethodPresent(), true);
    appendIndentedStringArrayField(
        json,
        10,
        "evidence_ids",
        application.mainMethodEvidenceIds(),
        false);
    indent(json, 9);
    json.append("},\n");
    appendIndentedStringArrayField(json, 9, "evidence_ids", application.evidenceIds(), false);
    indent(json, 8);
    json.append("}");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendApiSurface(
      StringBuilder json,
      ModuleAwareScan scan,
      OpenApiSpecDiscoveryAnalysis openApiSpecDiscoveryAnalysis,
      OpenApiOperationAnalysis openApiOperationAnalysis) {
    json.append("  \"api_surface\": {\n");
    appendIndentedStringField(
        json,
        2,
        "analysis_status",
        ANALYSIS_ANALYZED,
        true);
    appendEndpointCategorySection(
        json,
        "source_visible_spring_mvc_endpoints",
        endpointIdsForCategory(scan.endpoints(), API_SURFACE_CATEGORY_SOURCE_VISIBLE),
        true);
    appendEndpointCategorySection(
        json,
        "interface_declared_spring_mvc_endpoints",
        endpointIdsForCategory(scan.endpoints(), API_SURFACE_CATEGORY_INTERFACE_DECLARED),
        true);
    appendOpenApiSurface(json, openApiSpecDiscoveryAnalysis, openApiOperationAnalysis);
    appendWarningReferenceSection(
        json,
        "generated_source_api_signals",
        generatedSourceApiWarningIds(scan.warnings()),
        true);
    appendWarningReferenceSection(
        json,
        "repository_rest_warnings",
        repositoryRestWarningIds(scan.warnings()),
        true);
    appendWarningReferenceSection(
        json,
        "hidden_http_warnings",
        hiddenHttpWarningIds(scan.warnings()),
        false);
    json.append("  },\n");
  }

  private void appendEndpointCategorySection(
      StringBuilder json,
      String fieldName,
      List<String> endpointIds,
      boolean trailingComma) {
    indent(json, 2);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedStringField(json, 3, "analysis_status", ANALYSIS_ANALYZED, true);
    appendIndentedStringArrayField(json, 3, "endpoint_ids", endpointIds, false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private List<String> endpointIdsForCategory(
      List<ModuleScopedEndpointFact> endpoints,
      String apiSurfaceCategory) {
    return endpoints.stream()
        .filter(endpoint -> apiSurfaceCategory.equals(apiSurfaceCategory(endpoint.fact().mappingSource())))
        .map(endpoint -> endpointId(endpoint.moduleId(), endpoint.fact()))
        .toList();
  }

  private void appendOpenApiSurface(
      StringBuilder json,
      OpenApiSpecDiscoveryAnalysis openApiSpecDiscoveryAnalysis,
      OpenApiOperationAnalysis openApiOperationAnalysis) {
    indent(json, 2);
    json.append("\"openapi\": {\n");
    appendOpenApiSpecFiles(json, openApiSpecDiscoveryAnalysis);
    appendOpenApiOperations(json, openApiOperationAnalysis, false);
    indent(json, 2);
    json.append("},\n");
  }

  private void appendOpenApiSpecFiles(
      StringBuilder json,
      OpenApiSpecDiscoveryAnalysis openApiSpecDiscoveryAnalysis) {
    indent(json, 3);
    json.append("\"spec_files\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", openApiSpecDiscoveryAnalysis.analysisStatus(), true);
    indent(json, 4);
    json.append("\"items\": [");
    if (openApiSpecDiscoveryAnalysis.specFiles().isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("},\n");
      return;
    }

    json.append("\n");
    List<OpenApiSpecFileFact> specFiles = openApiSpecDiscoveryAnalysis.specFiles();
    for (int index = 0; index < specFiles.size(); index++) {
      appendOpenApiSpecFile(json, specFiles.get(index), index < specFiles.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("},\n");
  }

  private void appendOpenApiSpecFile(
      StringBuilder json,
      OpenApiSpecFileFact specFile,
      boolean trailingComma) {
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(json, 6, "id", specFile.id(), true);
    appendIndentedNullableStringField(json, 6, "module_id", specFile.moduleId(), true);
    appendIndentedStringField(json, 6, "spec_path", specFile.specPath(), true);
    appendIndentedStringField(json, 6, "format", specFile.format(), true);
    appendIndentedStringField(json, 6, "spec_kind", specFile.specKind(), true);
    appendIndentedNullableStringField(json, 6, "version", specFile.version(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", specFile.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendOpenApiOperations(
      StringBuilder json,
      OpenApiOperationAnalysis openApiOperationAnalysis,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"operations\": {\n");
    appendIndentedStringField(
        json,
        4,
        "analysis_status",
        openApiOperationAnalysis.analysisStatus(),
        true);
    indent(json, 4);
    json.append("\"items\": [");
    if (openApiOperationAnalysis.operations().isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < openApiOperationAnalysis.operations().size(); index++) {
      appendOpenApiOperation(
          json,
          openApiOperationAnalysis.operations().get(index),
          index < openApiOperationAnalysis.operations().size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendOpenApiOperation(
      StringBuilder json,
      OpenApiOperationFact operation,
      boolean trailingComma) {
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(json, 6, "id", operation.id(), true);
    appendIndentedNullableStringField(json, 6, "module_id", operation.moduleId(), true);
    appendIndentedStringField(
        json,
        6,
        "api_surface_category",
        operation.apiSurfaceCategory(),
        true);
    appendIndentedStringField(json, 6, "spec_path", operation.specPath(), true);
    appendIndentedStringField(json, 6, "http_method", operation.httpMethod(), true);
    appendIndentedStringField(json, 6, "path", operation.path(), true);
    appendIndentedNullableStringField(json, 6, "operation_id", operation.operationId(), true);
    appendIndentedStringArrayField(json, 6, "tags", operation.tags(), true);
    appendIndentedStringField(
        json,
        6,
        "implementation_status",
        operation.implementationStatus(),
        true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", operation.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendWarningReferenceSection(
      StringBuilder json,
      String fieldName,
      List<String> warningIds,
      boolean trailingComma) {
    indent(json, 2);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedStringField(json, 3, "analysis_status", ANALYSIS_ANALYZED, true);
    appendIndentedStringArrayField(json, 3, "warning_ids", warningIds, false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringApplicationSurface(StringBuilder json, ModuleAwareScan scan) {
    String analysisStatus = springApplicationSurfaceAnalysisStatus(scan);
    json.append("  \"spring_application_surface\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", analysisStatus, true);
    appendSpringRepositorySection(
        json,
        scan.springRepositories(),
        scan.springRepositoryAnalysisStatus(),
        true);
    appendSpringConfigurationSection(json, scan, scan.springConfigurationAnalysisStatus(), true);
    appendSpringBehaviorSection(json, scan, scan.springBehaviorAnalysisStatus(), true);
    appendSpringMessagingSection(json, scan, scan.springMessagingAnalysisStatus(), true);
    appendSpringSecurityShell(
        json,
        scan.springSecurityAnalysisStatus(),
        springSecurityWarningIds(scan.warnings()),
        false);
    json.append("  },\n");
  }

  private String springApplicationSurfaceAnalysisStatus(ModuleAwareScan scan) {
    if (ANALYSIS_ANALYZED.equals(scan.springRepositoryAnalysisStatus())
        || ANALYSIS_ANALYZED.equals(scan.springConfigurationAnalysisStatus())
        || ANALYSIS_ANALYZED.equals(scan.springBehaviorAnalysisStatus())
        || ANALYSIS_ANALYZED.equals(scan.springMessagingAnalysisStatus())
        || ANALYSIS_ANALYZED.equals(scan.springSecurityAnalysisStatus())) {
      return ANALYSIS_ANALYZED;
    }
    return ANALYSIS_NOT_DETECTED;
  }

  private void appendSpringRepositorySection(
      StringBuilder json,
      List<ModuleScopedSpringRepositoryFact> repositories,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"repositories\": {\n");
    appendIndentedStringField(json, 3, "analysis_status", analysisStatus, true);
    indent(json, 3);
    json.append("\"items\": [");
    if (repositories.isEmpty()) {
      json.append("]\n");
      indent(json, 2);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < repositories.size(); index++) {
      appendSpringRepository(json, repositories.get(index), index < repositories.size() - 1);
    }
    indent(json, 3);
    json.append("]\n");
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringRepository(
      StringBuilder json,
      ModuleScopedSpringRepositoryFact scopedRepository,
      boolean trailingComma) {
    SpringRepositoryFact repository = scopedRepository.fact();
    indent(json, 4);
    json.append("{\n");
    appendIndentedStringField(
        json,
        5,
        "id",
        springRepositoryId(scopedRepository.moduleId(), repository),
        true);
    appendIndentedStringField(json, 5, "module_id", scopedRepository.moduleId(), true);
    appendIndentedStringField(json, 5, "surface_category", repository.surfaceCategory(), true);
    appendIndentedStringField(json, 5, "support_type", repository.supportType(), true);
    appendIndentedStringField(json, 5, "class_name", repository.className(), true);
    appendIndentedStringField(json, 5, "source_path", repository.sourcePath(), true);
    appendIndentedStringField(json, 5, "repository_signal", repository.repositorySignal(), true);
    if (SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE.equals(
        repository.surfaceCategory())) {
      appendIndentedStringArrayField(json, 5, "extends_types", repository.extendsTypes(), true);
      appendIndentedStringField(
          json,
          5,
          "entity_relation_status",
          repository.entityRelationStatus(),
          true);
      appendSpringRepositoryEntityRelation(json, repository.entityRelation(), true);
    }
    appendIndentedStringArrayField(json, 5, "evidence_ids", repository.evidenceIds(), false);
    indent(json, 4);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringRepositoryEntityRelation(
      StringBuilder json,
      SpringRepositoryEntityRelationFact relation,
      boolean trailingComma) {
    indent(json, 5);
    json.append("\"entity_relation\": ");
    if (relation == null) {
      json.append("null");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("{\n");
    appendIndentedStringField(json, 6, "support_type", relation.supportType(), true);
    appendIndentedStringField(json, 6, "relation_type", relation.relationType(), true);
    appendIndentedStringField(json, 6, "target_entity_id", relation.targetEntityId(), true);
    appendIndentedStringField(json, 6, "target_module_id", relation.targetModuleId(), true);
    appendIndentedStringField(json, 6, "target_class_name", relation.targetClassName(), true);
    appendIndentedStringField(json, 6, "generic_type", relation.genericType(), true);
    appendIndentedStringField(json, 6, "confidence", relation.confidence(), true);
    appendIndentedNullableStringField(json, 6, "uncertainty", relation.uncertainty(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", relation.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringConfigurationSection(
      StringBuilder json,
      ModuleAwareScan scan,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"configuration\": {\n");
    appendSpringConfigurationClassesSection(
        json,
        scan.springConfigurationClasses(),
        analysisStatus,
        true);
    appendSpringConfigurationPropertiesSection(
        json,
        scan.springConfigurationProperties(),
        analysisStatus,
        true);
    appendSpringBeanMethodsSection(json, scan.springBeanMethods(), analysisStatus, false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringConfigurationClassesSection(
      StringBuilder json,
      List<ModuleScopedSpringConfigurationClassFact> configurationClasses,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"configuration_classes\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (configurationClasses.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < configurationClasses.size(); index++) {
      appendSpringConfigurationClass(
          json,
          configurationClasses.get(index),
          index < configurationClasses.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringConfigurationClass(
      StringBuilder json,
      ModuleScopedSpringConfigurationClassFact scopedConfiguration,
      boolean trailingComma) {
    SpringConfigurationClassFact configuration = scopedConfiguration.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springConfigurationClassId(scopedConfiguration.moduleId(), configuration),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedConfiguration.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", configuration.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", configuration.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", configuration.className(), true);
    appendIndentedStringField(json, 6, "source_path", configuration.sourcePath(), true);
    appendIndentedStringField(
        json,
        6,
        "configuration_signal",
        configuration.configurationSignal(),
        true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", configuration.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringConfigurationPropertiesSection(
      StringBuilder json,
      List<ModuleScopedSpringConfigurationPropertiesFact> configurationProperties,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"configuration_properties\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (configurationProperties.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < configurationProperties.size(); index++) {
      appendSpringConfigurationProperties(
          json,
          configurationProperties.get(index),
          index < configurationProperties.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringConfigurationProperties(
      StringBuilder json,
      ModuleScopedSpringConfigurationPropertiesFact scopedProperties,
      boolean trailingComma) {
    SpringConfigurationPropertiesFact properties = scopedProperties.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springConfigurationPropertiesId(scopedProperties.moduleId(), properties),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedProperties.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", properties.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", properties.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", properties.className(), true);
    appendIndentedStringField(json, 6, "source_path", properties.sourcePath(), true);
    appendIndentedStringField(
        json,
        6,
        "configuration_properties_signal",
        properties.configurationPropertiesSignal(),
        true);
    appendIndentedStringField(json, 6, "binding_status", properties.bindingStatus(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", properties.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringBeanMethodsSection(
      StringBuilder json,
      List<ModuleScopedSpringBeanMethodFact> beanMethods,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"bean_methods\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (beanMethods.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < beanMethods.size(); index++) {
      appendSpringBeanMethod(json, beanMethods.get(index), index < beanMethods.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringBeanMethod(
      StringBuilder json,
      ModuleScopedSpringBeanMethodFact scopedBeanMethod,
      boolean trailingComma) {
    SpringBeanMethodFact beanMethod = scopedBeanMethod.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springBeanMethodId(scopedBeanMethod.moduleId(), beanMethod),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedBeanMethod.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", beanMethod.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", beanMethod.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", beanMethod.className(), true);
    appendIndentedStringField(json, 6, "method_name", beanMethod.methodName(), true);
    appendIndentedStringField(json, 6, "source_path", beanMethod.sourcePath(), true);
    appendIndentedStringField(json, 6, "bean_signal", beanMethod.beanSignal(), true);
    appendIndentedStringField(json, 6, "bean_name_status", beanMethod.beanNameStatus(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", beanMethod.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringBehaviorSection(
      StringBuilder json,
      ModuleAwareScan scan,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"behavior\": {\n");
    appendSpringTransactionBoundariesSection(
        json,
        scan.springTransactionBoundaries(),
        analysisStatus,
        true);
    appendSpringScheduledMethodsSection(
        json,
        scan.springScheduledMethods(),
        analysisStatus,
        true);
    appendSpringEventListenersSection(
        json,
        scan.springEventListeners(),
        analysisStatus,
        false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringTransactionBoundariesSection(
      StringBuilder json,
      List<ModuleScopedSpringTransactionBoundaryFact> transactionBoundaries,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"transaction_boundaries\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (transactionBoundaries.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < transactionBoundaries.size(); index++) {
      appendSpringTransactionBoundary(
          json,
          transactionBoundaries.get(index),
          index < transactionBoundaries.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringTransactionBoundary(
      StringBuilder json,
      ModuleScopedSpringTransactionBoundaryFact scopedBoundary,
      boolean trailingComma) {
    SpringTransactionBoundaryFact boundary = scopedBoundary.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springTransactionBoundaryId(scopedBoundary.moduleId(), boundary),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedBoundary.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", boundary.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", boundary.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", boundary.className(), true);
    appendIndentedNullableStringField(json, 6, "method_name", boundary.methodName(), true);
    appendIndentedStringField(json, 6, "source_path", boundary.sourcePath(), true);
    appendIndentedStringField(json, 6, "target_kind", boundary.targetKind(), true);
    appendIndentedStringField(json, 6, "annotation_symbol", boundary.annotationSymbol(), true);
    appendIndentedStringField(json, 6, "transaction_signal", boundary.transactionSignal(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", boundary.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringScheduledMethodsSection(
      StringBuilder json,
      List<ModuleScopedSpringScheduledMethodFact> scheduledMethods,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"scheduled_methods\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (scheduledMethods.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < scheduledMethods.size(); index++) {
      appendSpringScheduledMethod(
          json,
          scheduledMethods.get(index),
          index < scheduledMethods.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringScheduledMethod(
      StringBuilder json,
      ModuleScopedSpringScheduledMethodFact scopedScheduledMethod,
      boolean trailingComma) {
    SpringScheduledMethodFact scheduledMethod = scopedScheduledMethod.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springScheduledMethodId(scopedScheduledMethod.moduleId(), scheduledMethod),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedScheduledMethod.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", scheduledMethod.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", scheduledMethod.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", scheduledMethod.className(), true);
    appendIndentedStringField(json, 6, "method_name", scheduledMethod.methodName(), true);
    appendIndentedStringField(json, 6, "source_path", scheduledMethod.sourcePath(), true);
    appendIndentedStringField(json, 6, "target_kind", scheduledMethod.targetKind(), true);
    appendIndentedStringField(json, 6, "annotation_symbol", scheduledMethod.annotationSymbol(), true);
    appendIndentedStringField(json, 6, "scheduled_signal", scheduledMethod.scheduledSignal(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", scheduledMethod.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringEventListenersSection(
      StringBuilder json,
      List<ModuleScopedSpringEventListenerFact> eventListeners,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"event_listeners\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (eventListeners.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < eventListeners.size(); index++) {
      appendSpringEventListener(
          json,
          eventListeners.get(index),
          index < eventListeners.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringEventListener(
      StringBuilder json,
      ModuleScopedSpringEventListenerFact scopedEventListener,
      boolean trailingComma) {
    SpringEventListenerFact eventListener = scopedEventListener.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springEventListenerId(scopedEventListener.moduleId(), eventListener),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedEventListener.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", eventListener.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", eventListener.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", eventListener.className(), true);
    appendIndentedStringField(json, 6, "method_name", eventListener.methodName(), true);
    appendIndentedStringField(json, 6, "source_path", eventListener.sourcePath(), true);
    appendIndentedStringField(json, 6, "target_kind", eventListener.targetKind(), true);
    appendIndentedStringField(json, 6, "annotation_symbol", eventListener.annotationSymbol(), true);
    appendIndentedStringField(json, 6, "event_listener_signal", eventListener.eventListenerSignal(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", eventListener.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringMessagingSection(
      StringBuilder json,
      ModuleAwareScan scan,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"messaging\": {\n");
    appendSpringMessagingListenersSection(
        json,
        scan.springMessagingListeners(),
        analysisStatus,
        false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringMessagingListenersSection(
      StringBuilder json,
      List<ModuleScopedSpringMessagingListenerFact> messagingListeners,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"listener_signals\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    indent(json, 4);
    json.append("\"items\": [");
    if (messagingListeners.isEmpty()) {
      json.append("]\n");
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < messagingListeners.size(); index++) {
      appendSpringMessagingListener(
          json,
          messagingListeners.get(index),
          index < messagingListeners.size() - 1);
    }
    indent(json, 4);
    json.append("]\n");
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringMessagingListener(
      StringBuilder json,
      ModuleScopedSpringMessagingListenerFact scopedMessagingListener,
      boolean trailingComma) {
    SpringMessagingListenerFact messagingListener = scopedMessagingListener.fact();
    indent(json, 5);
    json.append("{\n");
    appendIndentedStringField(
        json,
        6,
        "id",
        springMessagingListenerId(scopedMessagingListener.moduleId(), messagingListener),
        true);
    appendIndentedStringField(json, 6, "module_id", scopedMessagingListener.moduleId(), true);
    appendIndentedStringField(json, 6, "surface_category", messagingListener.surfaceCategory(), true);
    appendIndentedStringField(json, 6, "support_type", messagingListener.supportType(), true);
    appendIndentedStringField(json, 6, "class_name", messagingListener.className(), true);
    appendIndentedNullableStringField(json, 6, "method_name", messagingListener.methodName(), true);
    appendIndentedStringField(json, 6, "source_path", messagingListener.sourcePath(), true);
    appendIndentedStringField(json, 6, "target_kind", messagingListener.targetKind(), true);
    appendIndentedStringField(json, 6, "annotation_symbol", messagingListener.annotationSymbol(), true);
    appendIndentedStringField(json, 6, "listener_framework", messagingListener.listenerFramework(), true);
    appendIndentedStringField(json, 6, "listener_signal", messagingListener.listenerSignal(), true);
    appendIndentedStringArrayField(json, 6, "evidence_ids", messagingListener.evidenceIds(), false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSpringSecurityShell(
      StringBuilder json,
      String analysisStatus,
      List<String> warningIds,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"security\": {\n");
    indent(json, 3);
    json.append("\"configuration_warnings\": {\n");
    appendIndentedStringField(json, 4, "analysis_status", analysisStatus, true);
    appendIndentedStringArrayField(json, 4, "warning_ids", warningIds, false);
    indent(json, 3);
    json.append("}\n");
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private List<String> generatedSourceApiWarningIds(List<ModuleScopedWarningFact> warnings) {
    LinkedHashSet<String> openApiGeneratorContexts = warnings.stream()
        .filter(warning -> WARNING_CATEGORY_GENERATED_SOURCE.equals(warning.category()))
        .filter(warning -> WARNING_SIGNAL_MAVEN_OPENAPI_SWAGGER_CODEGEN_PLUGIN.equals(warning.signal()))
        .map(this::generatedSourceDeclarationContext)
        .flatMap(Optional::stream)
        .collect(
            LinkedHashSet::new,
            LinkedHashSet::add,
            LinkedHashSet::addAll);
    return warnings.stream()
        .filter(warning -> isGeneratedSourceApiWarning(warning, openApiGeneratorContexts))
        .map(ModuleScopedWarningFact::id)
        .toList();
  }

  private boolean isGeneratedSourceApiWarning(
      ModuleScopedWarningFact warning,
      LinkedHashSet<String> openApiGeneratorContexts) {
    if (WARNING_SIGNAL_MAVEN_OPENAPI_SWAGGER_CODEGEN_PLUGIN.equals(warning.signal())) {
      return true;
    }
    if (WARNING_CATEGORY_GENERATED_SOURCE.equals(warning.category())
        && WARNING_SIGNAL_GENERATED_SOURCE_ROOT_PATH_DETECTED.equals(warning.signal())) {
      return true;
    }
    if (WARNING_CATEGORY_GENERATED_SOURCE.equals(warning.category())
        && WARNING_SIGNAL_MAVEN_GENERATED_SOURCE_CONFIG.equals(warning.signal())) {
      return generatedSourceDeclarationContext(warning)
          .filter(openApiGeneratorContexts::contains)
          .isPresent();
    }
    return false;
  }

  private Optional<String> generatedSourceDeclarationContext(ModuleScopedWarningFact warning) {
    String prefix = "warning:"
        + WARNING_CATEGORY_GENERATED_SOURCE
        + ":"
        + warning.signal()
        + ":";
    if (!warning.id().startsWith(prefix)) {
      return Optional.empty();
    }
    return Optional.of(warning.id().substring(prefix.length()));
  }

  private List<String> repositoryRestWarningIds(List<ModuleScopedWarningFact> warnings) {
    return warnings.stream()
        .filter(warning -> WARNING_CATEGORY_HIDDEN_HTTP_SURFACE.equals(warning.category()))
        .filter(warning -> WARNING_SIGNAL_REPOSITORY_REST_RESOURCE.equals(warning.signal()))
        .map(ModuleScopedWarningFact::id)
        .toList();
  }

  private List<String> hiddenHttpWarningIds(List<ModuleScopedWarningFact> warnings) {
    return warnings.stream()
        .filter(warning -> WARNING_CATEGORY_HIDDEN_HTTP_SURFACE.equals(warning.category()))
        .filter(warning -> !WARNING_SIGNAL_OPENAPI_SPEC_FILE.equals(warning.signal()))
        .filter(warning -> !WARNING_SIGNAL_REPOSITORY_REST_RESOURCE.equals(warning.signal()))
        .filter(warning -> !WARNING_SIGNAL_MAVEN_OPENAPI_SWAGGER_CODEGEN_PLUGIN.equals(warning.signal()))
        .map(ModuleScopedWarningFact::id)
        .toList();
  }

  private List<String> springSecurityWarningIds(List<ModuleScopedWarningFact> warnings) {
    return warnings.stream()
        .filter(warning -> WARNING_CATEGORY_SPRING_SECURITY.equals(warning.category()))
        .map(ModuleScopedWarningFact::id)
        .toList();
  }

  private void appendEndpoint(
      StringBuilder json,
      ModuleScopedEndpointFact scopedEndpoint,
      boolean trailingComma) {
    SpringMvcEndpointFact endpoint = scopedEndpoint.fact();
    json.append("    {\n");
    appendIndentedStringField(json, 3, "id", endpointId(scopedEndpoint.moduleId(), endpoint), true);
    appendIndentedStringField(json, 3, "module_id", scopedEndpoint.moduleId(), true);
    appendIndentedStringField(
        json,
        3,
        "api_surface_category",
        apiSurfaceCategory(endpoint.mappingSource()),
        true);
    appendIndentedStringField(json, 3, "controller_class", endpoint.controllerClass(), true);
    appendIndentedStringField(json, 3, "handler_method", endpoint.handlerMethod(), true);
    appendIndentedStringArrayField(json, 3, "http_methods", endpoint.httpMethods(), true);
    appendIndentedStringField(
        json,
        3,
        "http_method_semantics",
        endpoint.httpMethodSemantics().name().toLowerCase(Locale.ROOT),
        true);
    appendIndentedStringArrayField(json, 3, "paths", endpoint.paths(), true);
    appendRequestParameters(json, endpoint.requestParameters());
    appendIndentedNullableStringField(
        json,
        3,
        "request_body_type",
        endpoint.requestBodyType(),
        true);
    appendIndentedNullableStringField(
        json,
        3,
        "response_type",
        endpoint.declaredResponseType(),
        true);
    appendMappingSource(json, endpoint.mappingSource());
    appendIndentedStringArrayField(json, 3, "evidence_ids", endpoint.evidenceIds(), false);
    json.append("    }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private String apiSurfaceCategory(SpringMvcEndpointMappingSource mappingSource) {
    if ("source_visible_interface_method".equals(mappingSource.kind())) {
      return API_SURFACE_CATEGORY_INTERFACE_DECLARED;
    }
    return API_SURFACE_CATEGORY_SOURCE_VISIBLE;
  }

  private void appendRequestParameters(
      StringBuilder json,
      List<SpringMvcRequestParameterFact> requestParameters) {
    json.append("      \"request_parameters\": [");
    if (requestParameters.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < requestParameters.size(); index++) {
      SpringMvcRequestParameterFact parameter = requestParameters.get(index);
      json.append("        {\n");
      appendIndentedStringField(json, 5, "name", parameter.name(), true);
      appendIndentedStringField(json, 5, "source", parameter.source(), true);
      appendIndentedStringField(json, 5, "java_type", parameter.javaType(), true);
      appendIndentedStringArrayField(json, 5, "evidence_ids", parameter.evidenceIds(), false);
      json.append("        }");
      if (index < requestParameters.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("      ],\n");
  }

  private void appendMappingSource(
      StringBuilder json,
      SpringMvcEndpointMappingSource mappingSource) {
    json.append("      \"mapping_source\": {\n");
    appendIndentedStringField(json, 4, "kind", mappingSource.kind(), true);
    appendIndentedStringField(json, 4, "declaring_type", mappingSource.declaringType(), true);
    appendIndentedStringField(json, 4, "declaring_method", mappingSource.declaringMethod(), true);
    appendIndentedStringField(json, 4, "binding", mappingSource.binding(), true);
    appendIndentedNullableStringField(json, 4, "uncertainty", mappingSource.uncertainty(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", mappingSource.evidenceIds(), false);
    json.append("      },\n");
  }

  private void appendComponents(StringBuilder json, List<ModuleScopedComponentFact> components) {
    json.append("    \"items\": [");
    if (components.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < components.size(); index++) {
      appendComponent(json, components.get(index), index < components.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendComponent(
      StringBuilder json,
      ModuleScopedComponentFact scopedComponent,
      boolean trailingComma) {
    SpringComponentFact component = scopedComponent.fact();
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", componentId(scopedComponent.moduleId(), component), true);
    appendIndentedStringField(json, 4, "module_id", scopedComponent.moduleId(), true);
    appendIndentedStringField(json, 4, "class_name", component.className(), true);
    appendIndentedStringArrayField(json, 4, "stereotypes", component.stereotypes(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", component.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendWarnings(StringBuilder json, List<ModuleScopedWarningFact> warnings) {
    json.append("    \"items\": [");
    if (warnings.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < warnings.size(); index++) {
      appendWarning(json, warnings.get(index), index < warnings.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendWarning(
      StringBuilder json,
      ModuleScopedWarningFact warning,
      boolean trailingComma) {
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", warning.id(), true);
    appendIndentedStringField(json, 4, "category", warning.category(), true);
    appendIndentedStringField(json, 4, "signal", warning.signal(), true);
    appendIndentedNullableStringField(json, 4, "module_id", warning.moduleId(), true);
    appendIndentedStringField(json, 4, "message", warning.message(), true);
    appendIndentedStringField(json, 4, "source_path", warning.sourcePath(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", warning.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendEntities(StringBuilder json, List<ModuleScopedEntityFact> entities) {
    json.append("    \"items\": [");
    if (entities.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < entities.size(); index++) {
      appendEntity(json, entities.get(index), index < entities.size() - 1);
    }
    json.append("    ],\n");
  }

  private void appendEmbeddables(
      StringBuilder json,
      List<ModuleScopedEmbeddableFact> embeddables,
      String analysisStatus) {
    json.append("    \"embeddables\": {\n");
    appendIndentedStringField(json, 3, "analysis_status", analysisStatus, true);
    json.append("      \"items\": [");
    if (embeddables.isEmpty()) {
      json.append("]\n");
      json.append("    }\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < embeddables.size(); index++) {
      appendEmbeddable(json, embeddables.get(index), index < embeddables.size() - 1);
    }
    json.append("      ]\n");
    json.append("    }\n");
  }

  private void appendEmbeddable(
      StringBuilder json,
      ModuleScopedEmbeddableFact scopedEmbeddable,
      boolean trailingComma) {
    JpaEmbeddableFact embeddable = scopedEmbeddable.fact();
    json.append("        {\n");
    appendIndentedStringField(json, 5, "id", embeddableId(scopedEmbeddable.moduleId(), embeddable), true);
    appendIndentedStringField(json, 5, "module_id", scopedEmbeddable.moduleId(), true);
    appendIndentedStringField(json, 5, "class_name", embeddable.className(), true);
    appendIndentedStringField(json, 5, "source_path", embeddable.sourcePath(), true);
    appendEntityFields(json, scopedEmbeddable.moduleId(), embeddable.fields(), 5);
    appendIndentedStringArrayField(json, 5, "evidence_ids", embeddable.evidenceIds(), false);
    json.append("        }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendEntity(
      StringBuilder json,
      ModuleScopedEntityFact scopedEntity,
      boolean trailingComma) {
    JpaEntityFact entity = scopedEntity.fact();
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", entityId(scopedEntity.moduleId(), entity), true);
    appendIndentedStringField(json, 4, "module_id", scopedEntity.moduleId(), true);
    appendIndentedStringField(json, 4, "class_name", entity.className(), true);
    appendIndentedNullableStringField(json, 4, "table_name", entity.tableName(), true);
    appendIdClass(json, entity.idClass());
    appendEntityFields(json, scopedEntity.moduleId(), entity.fields(), 4);
    appendIdentifierFields(json, entity.identifierFields());
    appendRelationships(json, entity.relationships());
    appendIndentedStringArrayField(json, 4, "evidence_ids", entity.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendIdClass(StringBuilder json, JpaIdClassFact idClass) {
    indent(json, 4);
    json.append("\"id_class\": ");
    if (idClass == null) {
      json.append("null,\n");
      return;
    }

    json.append("{\n");
    appendIndentedNullableStringField(json, 5, "type_name", idClass.typeName(), true);
    appendIndentedStringField(json, 5, "field_matching_status", idClass.fieldMatchingStatus(), true);
    appendIndentedStringField(
        json,
        5,
        "semantic_reconstruction_status",
        idClass.semanticReconstructionStatus(),
        true);
    appendIndentedStringArrayField(json, 5, "evidence_ids", idClass.evidenceIds(), false);
    indent(json, 4);
    json.append("},\n");
  }

  private void appendEntityFields(
      StringBuilder json,
      String moduleId,
      List<JpaEntityFieldFact> fields,
      int propertyIndentLevel) {
    indent(json, propertyIndentLevel);
    json.append("\"fields\": [");
    List<JpaEntityFieldFact> sortedFields = fields.stream()
        .sorted(ENTITY_FIELD_ORDER)
        .toList();
    if (sortedFields.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedFields.size(); index++) {
      JpaEntityFieldFact field = sortedFields.get(index);
      int itemIndentLevel = propertyIndentLevel + 1;
      int fieldIndentLevel = propertyIndentLevel + 2;
      indent(json, itemIndentLevel);
      json.append("{\n");
      appendIndentedStringField(json, fieldIndentLevel, "field_name", field.fieldName(), true);
      appendIndentedStringField(json, fieldIndentLevel, "java_type", field.javaType(), true);
      appendIndentedStringField(json, fieldIndentLevel, "declaring_class", field.declaringClass(), true);
      appendIndentedStringField(json, fieldIndentLevel, "source_kind", field.sourceKind(), true);
      appendIndentedStringField(json, fieldIndentLevel, "persistence_role", field.persistenceRole(), true);
      appendIndentedStringArrayField(json, fieldIndentLevel, "annotations", field.annotations(), true);
      appendColumn(json, field.column(), fieldIndentLevel);
      appendEnumerated(json, field.enumerated(), fieldIndentLevel);
      appendGeneratedValue(json, fieldIndentLevel, "generated_value", field.generatedValue(), true);
      appendVersion(json, field.version(), fieldIndentLevel);
      appendEmbedded(json, moduleId, field.embedded(), fieldIndentLevel);
      appendIndentedStringArrayField(json, fieldIndentLevel, "evidence_ids", field.evidenceIds(), false);
      indent(json, itemIndentLevel);
      json.append("}");
      if (index < sortedFields.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, propertyIndentLevel);
    json.append("],\n");
  }

  private void appendColumn(StringBuilder json, JpaColumnFact column, int indentLevel) {
    indent(json, indentLevel);
    json.append("\"column\": ");
    if (column == null) {
      json.append("null,\n");
      return;
    }

    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "name", column.name(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "nullable", column.nullable(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "unique", column.unique(), true);
    appendIndentedNullableIntegerField(json, indentLevel + 1, "length", column.length(), true);
    appendIndentedNullableIntegerField(json, indentLevel + 1, "precision", column.precision(), true);
    appendIndentedNullableIntegerField(json, indentLevel + 1, "scale", column.scale(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "insertable", column.insertable(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "updatable", column.updatable(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", column.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("},\n");
  }

  private void appendEnumerated(
      StringBuilder json,
      JpaEnumeratedFact enumerated,
      int indentLevel) {
    indent(json, indentLevel);
    json.append("\"enumerated\": ");
    if (enumerated == null) {
      json.append("null,\n");
      return;
    }

    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "value", enumerated.value(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", enumerated.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("},\n");
  }

  private void appendGeneratedValue(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      JpaGeneratedValueFact generatedValue,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": ");
    if (generatedValue == null) {
      json.append("null");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "strategy", generatedValue.strategy(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "generator", generatedValue.generator(), true);
    appendIndentedStringArrayField(
        json,
        indentLevel + 1,
        "evidence_ids",
        generatedValue.evidenceIds(),
        false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendVersion(
      StringBuilder json,
      JpaVersionFact version,
      int indentLevel) {
    indent(json, indentLevel);
    json.append("\"version\": ");
    if (version == null) {
      json.append("null,\n");
      return;
    }

    json.append("{\n");
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", version.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("},\n");
  }

  private void appendEmbedded(
      StringBuilder json,
      String moduleId,
      JpaEmbeddedFact embedded,
      int indentLevel) {
    indent(json, indentLevel);
    json.append("\"embedded\": ");
    if (embedded == null) {
      json.append("null,\n");
      return;
    }

    String targetClassName = embedded.targetClassName();
    json.append("{\n");
    appendIndentedStringField(json, indentLevel + 1, "annotation", embedded.annotation(), true);
    appendIndentedStringField(json, indentLevel + 1, "java_type", embedded.javaType(), true);
    appendIndentedStringField(json, indentLevel + 1, "target_resolution", embedded.targetResolution(), true);
    appendIndentedNullableStringField(
        json,
        indentLevel + 1,
        "target_embeddable_id",
        targetClassName == null ? null : embeddableId(moduleId, targetClassName),
        true);
    appendIndentedNullableStringField(
        json,
        indentLevel + 1,
        "target_module_id",
        targetClassName == null ? null : moduleId,
        true);
    appendIndentedNullableStringField(json, indentLevel + 1, "target_class_name", targetClassName, true);
    appendIndentedNullableStringField(json, indentLevel + 1, "support_type", embedded.supportType(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "confidence", embedded.confidence(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "uncertainty", embedded.uncertainty(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", embedded.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("},\n");
  }

  private void appendIdentifierFields(
      StringBuilder json,
      List<JpaIdentifierFieldFact> identifierFields) {
    json.append("        \"identifier_fields\": [");
    List<JpaIdentifierFieldFact> sortedIdentifierFields = identifierFields.stream()
        .sorted(IDENTIFIER_FIELD_ORDER)
        .toList();
    if (sortedIdentifierFields.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedIdentifierFields.size(); index++) {
      JpaIdentifierFieldFact identifierField = sortedIdentifierFields.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "field_name", identifierField.fieldName(), true);
      appendIndentedStringField(json, 6, "java_type", identifierField.javaType(), true);
      appendIndentedStringField(json, 6, "declaring_class", identifierField.declaringClass(), true);
      appendIndentedStringField(json, 6, "source_kind", identifierField.sourceKind(), true);
      appendIndentedStringField(json, 6, "identifier_kind", identifierField.identifierKind(), true);
      appendGeneratedValue(json, 6, "generated_value", identifierField.generatedValue(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", identifierField.evidenceIds(), false);
      json.append("          }");
      if (index < sortedIdentifierFields.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendRelationships(
      StringBuilder json,
      List<JpaRelationshipFact> relationships) {
    json.append("        \"relationships\": [");
    List<JpaRelationshipFact> sortedRelationships = relationships.stream()
        .sorted(RELATIONSHIP_ORDER)
        .toList();
    if (sortedRelationships.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedRelationships.size(); index++) {
      JpaRelationshipFact relationship = sortedRelationships.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "field_name", relationship.fieldName(), true);
      appendIndentedStringField(json, 6, "annotation", relationship.annotation(), true);
      appendIndentedStringField(json, 6, "cardinality", relationship.cardinality(), true);
      appendIndentedStringField(json, 6, "java_type", relationship.javaType(), true);
      appendRelationshipTarget(json, relationship.target(), 6);
      appendIndentedNullableStringField(json, 6, "mapped_by", relationship.mappedBy(), true);
      appendIndentedStringField(json, 6, "ownership_signal", relationship.ownershipSignal(), true);
      appendIndentedNullableBooleanField(json, 6, "optional", relationship.optional(), true);
      appendIndentedNullableStringField(json, 6, "fetch", relationship.fetch(), true);
      appendIndentedStringArrayField(json, 6, "cascade", relationship.cascade(), true);
      appendIndentedNullableBooleanField(json, 6, "orphan_removal", relationship.orphanRemoval(), true);
      appendJoinColumns(json, 6, "join_columns", relationship.joinColumns(), true);
      appendJoinTable(json, 6, relationship.joinTable(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", relationship.evidenceIds(), false);
      json.append("          }");
      if (index < sortedRelationships.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendRelationshipTarget(
      StringBuilder json,
      JpaRelationshipTargetFact target,
      int indentLevel) {
    indent(json, indentLevel);
    json.append("\"target\": {\n");
    appendIndentedStringField(json, indentLevel + 1, "declared_type", target.declaredType(), true);
    appendIndentedStringField(json, indentLevel + 1, "target_resolution", target.targetResolution(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "target_entity_id", target.targetEntityId(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "target_module_id", target.targetModuleId(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "target_class_name", target.targetClassName(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "support_type", target.supportType(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "confidence", target.confidence(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "uncertainty", target.uncertainty(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", target.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("},\n");
  }

  private void appendJoinColumns(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      List<JpaJoinColumnFact> joinColumns,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": [");
    if (joinColumns.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < joinColumns.size(); index++) {
      appendJoinColumn(json, indentLevel + 1, joinColumns.get(index), index < joinColumns.size() - 1);
    }
    indent(json, indentLevel);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendJoinColumn(
      StringBuilder json,
      int indentLevel,
      JpaJoinColumnFact joinColumn,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "name", joinColumn.name(), true);
    appendIndentedNullableStringField(
        json,
        indentLevel + 1,
        "referenced_column_name",
        joinColumn.referencedColumnName(),
        true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "nullable", joinColumn.nullable(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "unique", joinColumn.unique(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "insertable", joinColumn.insertable(), true);
    appendIndentedNullableBooleanField(json, indentLevel + 1, "updatable", joinColumn.updatable(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", joinColumn.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("}");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendJoinTable(
      StringBuilder json,
      int indentLevel,
      JpaJoinTableFact joinTable,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("\"join_table\": ");
    if (joinTable == null) {
      json.append("null");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("{\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "name", joinTable.name(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "schema", joinTable.schema(), true);
    appendIndentedNullableStringField(json, indentLevel + 1, "catalog", joinTable.catalog(), true);
    appendJoinColumns(json, indentLevel + 1, "join_columns", joinTable.joinColumns(), true);
    appendJoinColumns(json, indentLevel + 1, "inverse_join_columns", joinTable.inverseJoinColumns(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", joinTable.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendTests(StringBuilder json, List<ModuleScopedTestFact> tests) {
    json.append("    \"items\": [");
    if (tests.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < tests.size(); index++) {
      appendTest(json, tests.get(index), index < tests.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendTest(
      StringBuilder json,
      ModuleScopedTestFact scopedTest,
      boolean trailingComma) {
    TestClassFact test = scopedTest.fact();
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", testId(scopedTest.moduleId(), test), true);
    appendIndentedStringField(json, 4, "module_id", scopedTest.moduleId(), true);
    appendIndentedStringField(json, 4, "class_name", test.className(), true);
    appendIndentedStringField(json, 4, "source_path", test.sourcePath(), true);
    appendFrameworkSignals(json, test.frameworkSignals());
    appendSpringTestSlices(json, test.springTestSlices());
    appendMockSignals(json, test.mockSignals());
    appendTestMethods(json, test.methods());
    appendTestedSubjects(json, scopedTest.moduleId(), test.testedSubjects());
    appendIndentedStringArrayField(json, 4, "evidence_ids", test.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendFrameworkSignals(
      StringBuilder json,
      List<TestFrameworkSignalFact> frameworkSignals) {
    json.append("        \"framework_signals\": [");
    List<TestFrameworkSignalFact> sortedSignals = frameworkSignals.stream()
        .sorted(TEST_FRAMEWORK_SIGNAL_ORDER)
        .toList();
    if (sortedSignals.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedSignals.size(); index++) {
      TestFrameworkSignalFact signal = sortedSignals.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "name", signal.name(), true);
      appendIndentedStringField(json, 6, "signal_kind", signal.signalKind(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", signal.evidenceIds(), false);
      json.append("          }");
      if (index < sortedSignals.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendTestMethods(
      StringBuilder json,
      List<TestMethodFact> methods) {
    json.append("        \"methods\": [");
    if (methods.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < methods.size(); index++) {
      TestMethodFact method = methods.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "method_name", method.methodName(), true);
      appendIndentedStringField(json, 6, "test_annotation", method.testAnnotation(), true);
      appendIndentedStringField(json, 6, "method_kind", method.methodKind(), true);
      appendIndentedNullableStringField(json, 6, "display_name", method.displayName(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", method.evidenceIds(), false);
      json.append("          }");
      if (index < methods.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendSpringTestSlices(
      StringBuilder json,
      List<TestSpringSliceFact> slices) {
    json.append("        \"spring_test_slices\": [");
    List<TestSpringSliceFact> sortedSlices = slices.stream()
        .sorted(TEST_SPRING_SLICE_ORDER)
        .toList();
    if (sortedSlices.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedSlices.size(); index++) {
      TestSpringSliceFact slice = sortedSlices.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "annotation", slice.annotation(), true);
      appendIndentedStringField(json, 6, "slice_kind", slice.sliceKind(), true);
      appendIndentedStringField(json, 6, "signal_kind", slice.signalKind(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", slice.evidenceIds(), false);
      json.append("          }");
      if (index < sortedSlices.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendMockSignals(
      StringBuilder json,
      List<TestMockSignalFact> mockSignals) {
    json.append("        \"mock_signals\": [");
    List<TestMockSignalFact> sortedSignals = mockSignals.stream()
        .sorted(TEST_MOCK_SIGNAL_ORDER)
        .toList();
    if (sortedSignals.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedSignals.size(); index++) {
      TestMockSignalFact signal = sortedSignals.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "annotation", signal.annotation(), true);
      appendIndentedStringField(json, 6, "mock_signal", signal.mockSignal(), true);
      appendIndentedStringField(json, 6, "signal_kind", signal.signalKind(), true);
      appendIndentedStringField(json, 6, "target_kind", signal.targetKind(), true);
      appendIndentedStringField(json, 6, "target_name", signal.targetName(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", signal.evidenceIds(), false);
      json.append("          }");
      if (index < sortedSignals.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendTestedSubjects(
      StringBuilder json,
      String targetModuleId,
      List<TestedSubjectFact> testedSubjects) {
    json.append("        \"tested_subjects\": [");
    List<TestedSubjectFact> sortedSubjects = testedSubjects.stream()
        .sorted(TESTED_SUBJECT_ORDER)
        .toList();
    if (sortedSubjects.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedSubjects.size(); index++) {
      TestedSubjectFact subject = sortedSubjects.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "relation_status", subject.relationStatus(), true);
      appendIndentedStringField(json, 6, "relation_type", subject.relationType(), true);
      appendIndentedNullableStringField(json, 6, "class_name", subject.className(), true);
      appendIndentedNullableStringField(
          json,
          6,
          "target_module_id",
          subject.className() == null
              ? null
              : subject.targetModuleId() == null ? targetModuleId : subject.targetModuleId(),
          true);
      appendIndentedNullableStringField(json, 6, "candidate_reference", subject.candidateReference(), true);
      appendIndentedNullableStringField(json, 6, "support_type", subject.supportType(), true);
      appendIndentedStringField(json, 6, "confidence", subject.confidence(), true);
      appendIndentedNullableStringField(json, 6, "uncertainty", subject.uncertainty(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", subject.evidenceIds(), false);
      json.append("          }");
      if (index < sortedSubjects.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private QualitySignals qualitySignals(ModuleAwareScan scan) {
    List<TestGapSignal> testGapSignals = testGapSignals(scan).stream()
        .sorted(TEST_GAP_SIGNAL_ORDER)
        .toList();
    List<ChangeRiskSignal> changeRiskSignals = changeRiskSignals(scan).stream()
        .sorted(CHANGE_RISK_SIGNAL_ORDER)
        .toList();
    String analysisStatus = testGapSignals.isEmpty() && changeRiskSignals.isEmpty()
        ? ANALYSIS_NOT_DETECTED
        : ANALYSIS_ANALYZED;
    return new QualitySignals(analysisStatus, testGapSignals, changeRiskSignals);
  }

  private List<TestGapSignal> testGapSignals(ModuleAwareScan scan) {
    Set<SubjectKey> inferredSubjects = inferredSubjectKeys(scan.tests());
    List<TestGapSignal> signals = new ArrayList<>();
    for (ModuleScopedEndpointFact scopedEndpoint : scan.endpoints()) {
      SpringMvcEndpointFact endpoint = scopedEndpoint.fact();
      if (hasInferredSubject(inferredSubjects, scopedEndpoint.moduleId(), endpoint.controllerClass())) {
        continue;
      }
      String subjectId = endpointId(scopedEndpoint.moduleId(), endpoint);
      signals.add(testGapSignal(
          scopedEndpoint.moduleId(),
          scopedEndpoint.moduleOrder(),
          "endpoint_without_obvious_test",
          "spring_mvc_endpoint",
          subjectId,
          endpoint.controllerClass() + "#" + endpoint.handlerMethod(),
          endpoint.controllerClass(),
          endpoint.handlerMethod(),
          gapInferenceBasis(scan),
          gapUncertainty(scan),
          endpoint.evidenceIds()));
    }

    for (ModuleScopedSpringRepositoryFact scopedRepository : scan.springRepositories()) {
      SpringRepositoryFact repository = scopedRepository.fact();
      if (hasInferredSubject(inferredSubjects, scopedRepository.moduleId(), repository.className())) {
        continue;
      }
      String subjectId = springRepositoryId(scopedRepository.moduleId(), repository);
      signals.add(testGapSignal(
          scopedRepository.moduleId(),
          scopedRepository.moduleOrder(),
          "repository_without_obvious_test",
          "spring_repository",
          subjectId,
          repository.className(),
          repository.className(),
          null,
          gapInferenceBasis(scan),
          gapUncertainty(scan),
          repository.evidenceIds()));
    }

    for (ModuleScopedEntityFact scopedEntity : scan.entities()) {
      JpaEntityFact entity = scopedEntity.fact();
      if (hasInferredSubject(inferredSubjects, scopedEntity.moduleId(), entity.className())) {
        continue;
      }
      String subjectId = entityId(scopedEntity.moduleId(), entity);
      signals.add(testGapSignal(
          scopedEntity.moduleId(),
          scopedEntity.moduleOrder(),
          "entity_without_obvious_test",
          "jpa_entity",
          subjectId,
          entity.className(),
          entity.className(),
          null,
          gapInferenceBasis(scan),
          gapUncertainty(scan),
          entity.evidenceIds()));
    }
    return signals;
  }

  private TestGapSignal testGapSignal(
      String moduleId,
      int moduleOrder,
      String signal,
      String subjectKind,
      String subjectId,
      String subjectName,
      String subjectClassName,
      String subjectMemberName,
      String inferenceBasis,
      String uncertainty,
      List<String> evidenceIds) {
    return new TestGapSignal(
        "quality:test_gap:" + signal + ":" + subjectId,
        moduleId,
        moduleOrder,
        signal,
        QUALITY_STATUS_NO_OBVIOUS_TEST,
        subjectKind,
        subjectId,
        subjectName,
        subjectClassName,
        subjectMemberName,
        inferenceBasis,
        "low",
        uncertainty,
        List.of(),
        evidenceIds);
  }

  private String gapInferenceBasis(ModuleAwareScan scan) {
    if (ANALYSIS_ANALYZED.equals(scan.testAnalysisStatus())) {
      return "no_inferred_tested_subject_relation_for_subject_class";
    }
    return "bounded_test_inventory_not_available";
  }

  private String gapUncertainty(ModuleAwareScan scan) {
    if (ANALYSIS_ANALYZED.equals(scan.testAnalysisStatus())) {
      return QUALITY_UNCERTAINTY_BOUNDED_TEST_INVENTORY;
    }
    return QUALITY_UNCERTAINTY_TEST_ROOTS_NOT_DETECTED;
  }

  private Set<SubjectKey> inferredSubjectKeys(List<ModuleScopedTestFact> tests) {
    Set<SubjectKey> subjects = new LinkedHashSet<>();
    for (ModuleScopedTestFact scopedTest : tests) {
      for (TestedSubjectFact subject : scopedTest.fact().testedSubjects()) {
        if (!"inferred".equals(subject.relationStatus()) || subject.className() == null) {
          continue;
        }
        String targetModuleId = subject.targetModuleId() == null
            ? scopedTest.moduleId()
            : subject.targetModuleId();
        subjects.add(new SubjectKey(targetModuleId, subject.className()));
      }
    }
    return subjects;
  }

  private boolean hasInferredSubject(Set<SubjectKey> inferredSubjects, String moduleId, String className) {
    return inferredSubjects.contains(new SubjectKey(moduleId, className));
  }

  private List<ChangeRiskSignal> changeRiskSignals(ModuleAwareScan scan) {
    List<ChangeRiskSignal> signals = new ArrayList<>();
    for (ModuleScopedComponentFact scopedComponent : scan.components()) {
      SpringComponentFact component = scopedComponent.fact();
      if (!component.stereotypes().contains("@Service")) {
        continue;
      }
      String subjectId = componentId(scopedComponent.moduleId(), component);
      signals.add(changeRiskSignal(
          scopedComponent.moduleId(),
          scopedComponent.moduleOrder(),
          "spring_service_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_service",
          subjectId,
          component.className(),
          component.className(),
          null,
          "source_visible_service_stereotype",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          component.evidenceIds()));
    }

    for (ModuleScopedSpringConfigurationClassFact scopedConfiguration : scan.springConfigurationClasses()) {
      SpringConfigurationClassFact configuration = scopedConfiguration.fact();
      String subjectId = springConfigurationClassId(scopedConfiguration.moduleId(), configuration);
      signals.add(changeRiskSignal(
          scopedConfiguration.moduleId(),
          scopedConfiguration.moduleOrder(),
          "spring_configuration_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_configuration_class",
          subjectId,
          configuration.className(),
          configuration.className(),
          null,
          "source_visible_spring_configuration",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          configuration.evidenceIds()));
    }

    for (ModuleScopedSpringConfigurationPropertiesFact scopedProperties
        : scan.springConfigurationProperties()) {
      SpringConfigurationPropertiesFact properties = scopedProperties.fact();
      String subjectId = springConfigurationPropertiesId(scopedProperties.moduleId(), properties);
      signals.add(changeRiskSignal(
          scopedProperties.moduleId(),
          scopedProperties.moduleOrder(),
          "spring_configuration_properties_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_configuration_properties",
          subjectId,
          properties.className(),
          properties.className(),
          null,
          "source_visible_configuration_properties",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          properties.evidenceIds()));
    }

    for (ModuleScopedSpringBeanMethodFact scopedBeanMethod : scan.springBeanMethods()) {
      SpringBeanMethodFact beanMethod = scopedBeanMethod.fact();
      String subjectId = springBeanMethodId(scopedBeanMethod.moduleId(), beanMethod);
      signals.add(changeRiskSignal(
          scopedBeanMethod.moduleId(),
          scopedBeanMethod.moduleOrder(),
          "spring_bean_method_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_bean_method",
          subjectId,
          beanMethod.className() + "#" + beanMethod.methodName(),
          beanMethod.className(),
          beanMethod.methodName(),
          "source_visible_bean_method",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          beanMethod.evidenceIds()));
    }

    for (ModuleScopedSpringTransactionBoundaryFact scopedBoundary
        : scan.springTransactionBoundaries()) {
      SpringTransactionBoundaryFact boundary = scopedBoundary.fact();
      String subjectId = springTransactionBoundaryId(scopedBoundary.moduleId(), boundary);
      signals.add(changeRiskSignal(
          scopedBoundary.moduleId(),
          scopedBoundary.moduleOrder(),
          "transaction_boundary_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_transaction_boundary",
          subjectId,
          behaviorSubjectName(boundary.className(), boundary.methodName()),
          boundary.className(),
          boundary.methodName(),
          "source_visible_transaction_boundary",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          boundary.evidenceIds()));
    }

    for (ModuleScopedSpringScheduledMethodFact scopedScheduledMethod : scan.springScheduledMethods()) {
      SpringScheduledMethodFact scheduledMethod = scopedScheduledMethod.fact();
      String subjectId = springScheduledMethodId(scopedScheduledMethod.moduleId(), scheduledMethod);
      signals.add(changeRiskSignal(
          scopedScheduledMethod.moduleId(),
          scopedScheduledMethod.moduleOrder(),
          "scheduled_method_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_scheduled_method",
          subjectId,
          scheduledMethod.className() + "#" + scheduledMethod.methodName(),
          scheduledMethod.className(),
          scheduledMethod.methodName(),
          "source_visible_scheduled_method",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          scheduledMethod.evidenceIds()));
    }

    for (ModuleScopedSpringEventListenerFact scopedEventListener : scan.springEventListeners()) {
      SpringEventListenerFact eventListener = scopedEventListener.fact();
      String subjectId = springEventListenerId(scopedEventListener.moduleId(), eventListener);
      signals.add(changeRiskSignal(
          scopedEventListener.moduleId(),
          scopedEventListener.moduleOrder(),
          "event_listener_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_event_listener",
          subjectId,
          eventListener.className() + "#" + eventListener.methodName(),
          eventListener.className(),
          eventListener.methodName(),
          "source_visible_event_listener",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          eventListener.evidenceIds()));
    }

    for (ModuleScopedSpringMessagingListenerFact scopedListener : scan.springMessagingListeners()) {
      SpringMessagingListenerFact listener = scopedListener.fact();
      String subjectId = springMessagingListenerId(scopedListener.moduleId(), listener);
      signals.add(changeRiskSignal(
          scopedListener.moduleId(),
          scopedListener.moduleOrder(),
          "messaging_listener_change_surface",
          QUALITY_STATUS_PLANNING_HINT,
          "spring_messaging_listener",
          subjectId,
          behaviorSubjectName(listener.className(), listener.methodName()),
          listener.className(),
          listener.methodName(),
          "source_visible_messaging_listener",
          QUALITY_UNCERTAINTY_SOURCE_VISIBLE_ONLY,
          listener.evidenceIds()));
    }

    for (ModuleScopedSpringRepositoryFact scopedRepository : scan.springRepositories()) {
      SpringRepositoryFact repository = scopedRepository.fact();
      if (!SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE.equals(
          repository.surfaceCategory())
          || "inferred".equals(repository.entityRelationStatus())) {
        continue;
      }
      String subjectId = springRepositoryId(scopedRepository.moduleId(), repository);
      signals.add(changeRiskSignal(
          scopedRepository.moduleId(),
          scopedRepository.moduleOrder(),
          "repository_entity_relation_uncertain",
          QUALITY_STATUS_UNCERTAIN_PLANNING_HINT,
          "spring_data_repository",
          subjectId,
          repository.className(),
          repository.className(),
          null,
          "repository_entity_relation_status_" + repository.entityRelationStatus(),
          "bounded_repository_entity_relation_rules_only",
          repository.evidenceIds()));
    }

    for (ModuleScopedEntityFact scopedEntity : scan.entities()) {
      JpaEntityFact entity = scopedEntity.fact();
      for (JpaRelationshipFact relationship : entity.relationships()) {
        String subjectId = entityId(scopedEntity.moduleId(), entity)
            + "#relationship:"
            + relationship.fieldName();
        signals.add(changeRiskSignal(
            scopedEntity.moduleId(),
            scopedEntity.moduleOrder(),
            "jpa_relationship_change_surface",
            QUALITY_STATUS_UNCERTAIN_PLANNING_HINT,
            "jpa_relationship",
            subjectId,
            entity.className() + "#" + relationship.fieldName(),
            entity.className(),
            relationship.fieldName(),
            "source_visible_jpa_relationship_metadata",
            "relationship_target_declared_type_only",
            relationship.evidenceIds()));
      }
    }

    for (ModuleScopedWarningFact warning : scan.warnings()) {
      if (!WARNING_CATEGORY_SPRING_SECURITY.equals(warning.category())) {
        continue;
      }
      signals.add(changeRiskSignal(
          warning.moduleId(),
          warning.moduleOrder(),
          "spring_security_warning_change_surface",
          QUALITY_STATUS_WARNING_ORIENTED_PLANNING_HINT,
          "spring_security_warning",
          warning.id(),
          warning.id(),
          null,
          null,
          "source_visible_spring_security_warning",
          "warning_signal_only_not_vulnerability_or_correctness",
          warning.evidenceIds()));
    }
    return signals;
  }

  private ChangeRiskSignal changeRiskSignal(
      String moduleId,
      int moduleOrder,
      String signal,
      String status,
      String subjectKind,
      String subjectId,
      String subjectName,
      String subjectClassName,
      String subjectMemberName,
      String riskBasis,
      String uncertainty,
      List<String> evidenceIds) {
    return new ChangeRiskSignal(
        "quality:change_risk:" + signal + ":" + subjectId,
        moduleId,
        moduleOrder,
        signal,
        status,
        subjectKind,
        subjectId,
        subjectName,
        subjectClassName,
        subjectMemberName,
        riskBasis,
        "low",
        uncertainty,
        evidenceIds);
  }

  private String behaviorSubjectName(String className, String methodName) {
    if (methodName == null || methodName.isBlank()) {
      return className;
    }
    return className + "#" + methodName;
  }

  private void appendQuality(StringBuilder json, QualitySignals qualitySignals) {
    json.append("  \"quality\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", qualitySignals.analysisStatus(), true);
    appendTestGapSignals(json, qualitySignals.testGapSignals(), true);
    appendChangeRiskSignals(json, qualitySignals.changeRiskSignals(), false);
    json.append("  }\n");
  }

  private void appendTestGapSignals(
      StringBuilder json,
      List<TestGapSignal> signals,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"test_gap_signals\": {\n");
    appendIndentedStringField(
        json,
        3,
        "analysis_status",
        signals.isEmpty() ? ANALYSIS_NOT_DETECTED : ANALYSIS_ANALYZED,
        true);
    indent(json, 3);
    json.append("\"items\": [");
    if (signals.isEmpty()) {
      json.append("]\n");
      indent(json, 2);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < signals.size(); index++) {
      appendTestGapSignal(json, signals.get(index), index < signals.size() - 1);
    }
    indent(json, 3);
    json.append("]\n");
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendTestGapSignal(
      StringBuilder json,
      TestGapSignal signal,
      boolean trailingComma) {
    indent(json, 4);
    json.append("{\n");
    appendQualitySignalCore(json, signal);
    appendIndentedStringField(json, 5, "inference_basis", signal.inferenceBasis(), true);
    appendIndentedStringField(json, 5, "confidence", signal.confidence(), true);
    appendIndentedStringField(json, 5, "uncertainty", signal.uncertainty(), true);
    appendIndentedStringArrayField(json, 5, "related_test_ids", signal.relatedTestIds(), true);
    appendIndentedStringArrayField(json, 5, "evidence_ids", signal.evidenceIds(), false);
    indent(json, 4);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendChangeRiskSignals(
      StringBuilder json,
      List<ChangeRiskSignal> signals,
      boolean trailingComma) {
    indent(json, 2);
    json.append("\"change_risk_signals\": {\n");
    appendIndentedStringField(
        json,
        3,
        "analysis_status",
        signals.isEmpty() ? ANALYSIS_NOT_DETECTED : ANALYSIS_ANALYZED,
        true);
    indent(json, 3);
    json.append("\"items\": [");
    if (signals.isEmpty()) {
      json.append("]\n");
      indent(json, 2);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < signals.size(); index++) {
      appendChangeRiskSignal(json, signals.get(index), index < signals.size() - 1);
    }
    indent(json, 3);
    json.append("]\n");
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendChangeRiskSignal(
      StringBuilder json,
      ChangeRiskSignal signal,
      boolean trailingComma) {
    indent(json, 4);
    json.append("{\n");
    appendQualitySignalCore(json, signal);
    appendIndentedStringField(json, 5, "risk_basis", signal.riskBasis(), true);
    appendIndentedStringField(json, 5, "confidence", signal.confidence(), true);
    appendIndentedStringField(json, 5, "uncertainty", signal.uncertainty(), true);
    appendIndentedStringArrayField(json, 5, "evidence_ids", signal.evidenceIds(), false);
    indent(json, 4);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendQualitySignalCore(StringBuilder json, QualitySignal signal) {
    appendIndentedStringField(json, 5, "id", signal.id(), true);
    appendIndentedStringField(json, 5, "module_id", signal.moduleId(), true);
    appendIndentedStringField(json, 5, "signal", signal.signal(), true);
    appendIndentedStringField(json, 5, "status", signal.status(), true);
    appendIndentedStringField(json, 5, "subject_kind", signal.subjectKind(), true);
    appendIndentedStringField(json, 5, "subject_id", signal.subjectId(), true);
    appendIndentedStringField(json, 5, "subject_name", signal.subjectName(), true);
    appendIndentedNullableStringField(
        json,
        5,
        "subject_class_name",
        signal.subjectClassName(),
        true);
    appendIndentedNullableStringField(
        json,
        5,
        "subject_member_name",
        signal.subjectMemberName(),
        true);
  }

  private static String endpointId(String moduleId, SpringMvcEndpointFact endpoint) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return "endpoint:" + endpoint.controllerClass() + "#" + endpoint.handlerMethod();
    }
    return "endpoint:" + moduleId + ":" + endpoint.controllerClass() + "#" + endpoint.handlerMethod();
  }

  private static String testId(String moduleId, TestClassFact test) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return "test:" + test.className();
    }
    return "test:" + moduleId + ":" + test.className();
  }

  private static String componentId(String moduleId, SpringComponentFact component) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return component.id();
    }
    return "component:" + moduleId + ":" + component.className();
  }

  private static String springRepositoryId(String moduleId, SpringRepositoryFact repository) {
    return repository.surfaceCategory() + ":" + moduleId + ":" + repository.className();
  }

  private static String springConfigurationClassId(
      String moduleId,
      SpringConfigurationClassFact configuration) {
    return configuration.surfaceCategory() + ":" + moduleId + ":" + configuration.className();
  }

  private static String springConfigurationPropertiesId(
      String moduleId,
      SpringConfigurationPropertiesFact properties) {
    return properties.surfaceCategory() + ":" + moduleId + ":" + properties.className();
  }

  private static String springBeanMethodId(String moduleId, SpringBeanMethodFact beanMethod) {
    return beanMethod.surfaceCategory()
        + ":"
        + moduleId
        + ":"
        + beanMethod.className()
        + "#"
        + beanMethod.methodName()
        + ":"
        + beanMethod.idDiscriminator();
  }

  private static String springTransactionBoundaryId(
      String moduleId,
      SpringTransactionBoundaryFact boundary) {
    return springBehaviorTargetId(
        boundary.surfaceCategory(),
        moduleId,
        boundary.className(),
        boundary.methodName(),
        boundary.idDiscriminator());
  }

  private static String springScheduledMethodId(
      String moduleId,
      SpringScheduledMethodFact scheduledMethod) {
    return springBehaviorTargetId(
        scheduledMethod.surfaceCategory(),
        moduleId,
        scheduledMethod.className(),
        scheduledMethod.methodName(),
        scheduledMethod.idDiscriminator());
  }

  private static String springEventListenerId(
      String moduleId,
      SpringEventListenerFact eventListener) {
    return springBehaviorTargetId(
        eventListener.surfaceCategory(),
        moduleId,
        eventListener.className(),
        eventListener.methodName(),
        eventListener.idDiscriminator());
  }

  private static String springMessagingListenerId(
      String moduleId,
      SpringMessagingListenerFact listener) {
    return springBehaviorTargetId(
        listener.surfaceCategory(),
        moduleId,
        listener.className(),
        listener.methodName(),
        listener.idDiscriminator());
  }

  private static String springBehaviorTargetId(
      String surfaceCategory,
      String moduleId,
      String className,
      String methodName,
      String idDiscriminator) {
    String owner = methodName == null ? className : className + "#" + methodName;
    return surfaceCategory + ":" + moduleId + ":" + owner + ":" + idDiscriminator;
  }

  private static String entityId(String moduleId, JpaEntityFact entity) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return entity.id();
    }
    return "entity:" + moduleId + ":" + entity.className();
  }

  private static String embeddableId(String moduleId, JpaEmbeddableFact embeddable) {
    return embeddableId(moduleId, embeddable.className());
  }

  private static String embeddableId(String moduleId, String className) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return "embeddable:" + className;
    }
    return "embeddable:" + moduleId + ":" + className;
  }

  private List<EndpointRow> endpointRows(List<ModuleScopedEndpointFact> endpoints) {
    List<EndpointRow> rows = new ArrayList<>();
    for (ModuleScopedEndpointFact scopedEndpoint : endpoints) {
      SpringMvcEndpointFact endpoint = scopedEndpoint.fact();
      List<String> methodLabels = endpointMethodLabels(endpoint);
      for (String path : endpoint.paths()) {
        for (String methodLabel : methodLabels) {
          rows.add(new EndpointRow(
              scopedEndpoint.moduleId(),
              scopedEndpoint.moduleOrder(),
              methodLabel,
              path,
              endpoint));
        }
      }
    }

    return rows.stream()
        .sorted(Comparator.comparingInt(EndpointRow::moduleOrder)
            .thenComparing(EndpointRow::path)
            .thenComparing(EndpointRow::methodLabel)
            .thenComparing(row -> row.endpoint().controllerClass())
            .thenComparing(row -> row.endpoint().handlerMethod()))
        .toList();
  }

  private String moduleLabel(String moduleId, MavenModuleItem module) {
    if (moduleId == null || moduleId.isBlank()) {
      return code("unscoped") + " (module path not recorded)";
    }
    if (module == null) {
      return code(moduleId) + " (module path not recorded)";
    }
    return code(moduleId) + " (" + code(module.modulePath()) + ")";
  }

  private List<String> endpointMethodLabels(SpringMvcEndpointFact endpoint) {
    if (endpoint.httpMethodSemantics() == SpringMvcHttpMethodSemantics.DECLARED
        && !endpoint.httpMethods().isEmpty()) {
      return endpoint.httpMethods();
    }

    return switch (endpoint.httpMethodSemantics()) {
      case NOT_DECLARED -> List.of("METHOD NOT DECLARED");
      case UNSUPPORTED -> List.of("METHOD UNSUPPORTED");
      case DECLARED -> List.of("METHOD NOT DETECTED");
    };
  }

  private String httpMethods(SpringMvcEndpointFact endpoint) {
    if (endpoint.httpMethodSemantics() == SpringMvcHttpMethodSemantics.DECLARED
        && !endpoint.httpMethods().isEmpty()) {
      return codeList(endpoint.httpMethods());
    }

    return switch (endpoint.httpMethodSemantics()) {
      case NOT_DECLARED -> "not declared in source";
      case UNSUPPORTED -> "unsupported source expression";
      case DECLARED -> "not detected";
    };
  }

  private String requestParameters(List<SpringMvcRequestParameterFact> requestParameters) {
    if (requestParameters.isEmpty()) {
      return "none detected";
    }

    StringJoiner joiner = new StringJoiner(", ");
    for (SpringMvcRequestParameterFact parameter : requestParameters) {
      joiner.add(code(parameter.source() + ":" + parameter.name())
          + " ("
          + code(parameter.javaType())
          + ")");
    }
    return joiner.toString();
  }

  private String mappingSourceLabel(SpringMvcEndpointMappingSource mappingSource) {
    return code(mappingSource.kind())
        + " ("
        + code(mappingSource.declaringType() + "#" + mappingSource.declaringMethod())
        + ")";
  }

  private List<EvidenceRecord> evidenceRecords(
      ProjectLayout layout,
      List<MavenModuleDiscoveryEvidence> moduleEvidenceRecords,
      List<MavenMetadataEvidence> metadataEvidenceRecords,
      List<MavenDependencyEvidence> dependencyEvidenceRecords,
      List<MavenPluginEvidence> pluginEvidenceRecords,
      List<ResourceConfigEvidence> resourceConfigEvidenceRecords,
      List<SpringBootApplicationEvidence> springBootApplicationEvidenceRecords,
      List<ApiSpecEvidence> apiSpecEvidenceRecords,
      List<ApiSpecEvidence> openApiOperationEvidenceRecords,
      List<SpringMvcEndpointEvidence> endpointEvidenceRecords,
      List<SpringComponentEvidence> componentEvidenceRecords,
      List<SpringRepositoryEvidence> springRepositoryEvidenceRecords,
      List<SpringConfigurationEvidence> springConfigurationEvidenceRecords,
      List<SpringBehaviorEvidence> springBehaviorEvidenceRecords,
      List<JpaEntityEvidence> entityEvidenceRecords,
      List<TestInventoryEvidence> testEvidenceRecords,
      List<AnalysisWarningEvidence> warningEvidenceRecords) {
    Map<String, EvidenceRecord> uniqueRecords = new LinkedHashMap<>();
    layout.buildFileEvidence().ifPresent(evidence -> uniqueRecords.put(evidence.id(), evidence));
    moduleEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    metadataEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    dependencyEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    pluginEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    resourceConfigEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    springBootApplicationEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    apiSpecEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    openApiOperationEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    endpointEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    componentEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    springRepositoryEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    springConfigurationEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    springBehaviorEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    entityEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    testEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    warningEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));

    return uniqueRecords.values().stream()
        .sorted(EVIDENCE_ORDER)
        .toList();
  }

  private EvidenceRecord evidenceRecord(SpringMvcEndpointEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenModuleDiscoveryEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenMetadataEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenDependencyEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenPluginEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(ResourceConfigEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(SpringBootApplicationEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(ApiSpecEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(SpringComponentEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        ANNOTATION_SOURCE_TYPE,
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.annotationSymbol(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(SpringRepositoryEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(SpringConfigurationEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(SpringBehaviorEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(JpaEntityEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        ANNOTATION_SOURCE_TYPE,
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.annotationSymbol(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(TestInventoryEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(AnalysisWarningEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private String evidenceIndexJsonl(List<EvidenceRecord> evidenceRecords) {
    StringBuilder jsonl = new StringBuilder();
    for (EvidenceRecord evidence : evidenceRecords) {
      jsonl.append("{");
      appendStringField(jsonl, "id", evidence.id());
      appendStringField(jsonl, "source_type", evidence.sourceType());
      appendStringField(jsonl, "path", evidence.path());
      appendNullableStringField(jsonl, "class_name", evidence.className());
      appendNullableStringField(jsonl, "method_name", evidence.methodName());
      appendStringField(jsonl, "symbol_name", evidence.symbolName());
      appendNullableIntegerField(jsonl, "line_start", evidence.lineStart());
      appendNullableIntegerField(jsonl, "line_end", evidence.lineEnd());
      appendStringField(jsonl, "excerpt", evidence.excerpt());
      appendStringField(jsonl, "confidence", evidence.confidence());
      jsonl.append("}\n");
    }
    return jsonl.toString();
  }

  private void appendIndentedStringField(
      StringBuilder json,
      int indentLevel,
      String name,
      String value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(jsonString(value));
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedNullableStringField(
      StringBuilder json,
      int indentLevel,
      String name,
      String value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append(jsonString(value));
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedIntegerField(
      StringBuilder json,
      int indentLevel,
      String name,
      int value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(value);
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedBooleanField(
      StringBuilder json,
      int indentLevel,
      String name,
      boolean value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(value);
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedNullableIntegerField(
      StringBuilder json,
      int indentLevel,
      String name,
      Integer value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append(value);
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedNullableBooleanField(
      StringBuilder json,
      int indentLevel,
      String name,
      Boolean value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append(value);
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedStringArrayField(
      StringBuilder json,
      int indentLevel,
      String name,
      List<String> values,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ");
    appendStringArray(json, indentLevel, values);
    appendLineEnding(json, trailingComma);
  }

  private void appendStringArray(StringBuilder json, int indentLevel, List<String> values) {
    if (values.isEmpty()) {
      json.append("[]");
      return;
    }

    json.append("[\n");
    for (int index = 0; index < values.size(); index++) {
      indent(json, indentLevel + 1);
      json.append(jsonString(values.get(index)));
      if (index < values.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, indentLevel);
    json.append("]");
  }

  private void appendLineEnding(StringBuilder json, boolean trailingComma) {
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void indent(StringBuilder json, int indentLevel) {
    json.append("  ".repeat(indentLevel));
  }

  private void appendStringField(StringBuilder json, String name, String value) {
    appendFieldPrefix(json, name);
    json.append(jsonString(value));
  }

  private void appendNullableStringField(StringBuilder json, String name, String value) {
    appendFieldPrefix(json, name);
    if (value == null) {
      json.append("null");
      return;
    }
    json.append(jsonString(value));
  }

  private void appendNullableIntegerField(StringBuilder json, String name, Integer value) {
    appendFieldPrefix(json, name);
    if (value == null) {
      json.append("null");
      return;
    }
    json.append(value);
  }

  private void appendFieldPrefix(StringBuilder json, String name) {
    if (json.charAt(json.length() - 1) != '{') {
      json.append(",");
    }
    json.append(jsonString(name)).append(":");
  }

  private String jsonString(String value) {
    StringBuilder escaped = new StringBuilder();
    escaped.append('"');
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      switch (character) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (character == 0x2028) {
            escaped.append("\\u2028");
          } else if (character == 0x2029) {
            escaped.append("\\u2029");
          } else if (character < 0x20) {
            escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
          } else {
            escaped.append(character);
          }
        }
      }
    }
    escaped.append('"');
    return escaped.toString();
  }

  private String nullableCode(String value) {
    if (value == null || value.isBlank()) {
      return "none detected";
    }
    return code(value);
  }

  private String codeList(List<String> values) {
    return MarkdownRenderer.inlineCodeList(values, "none detected");
  }

  private String code(String value) {
    return MarkdownRenderer.inlineCode(value);
  }

  private static String firstPath(SpringMvcEndpointFact endpoint) {
    if (endpoint.paths().isEmpty()) {
      return "";
    }
    return endpoint.paths().get(0);
  }

  private static String nullSafe(String value) {
    if (value == null) {
      return "";
    }
    return value;
  }

  private static String ordinalText(int ordinal) {
    return String.format("%06d", ordinal);
  }

  private void writeGeneratedFiles(
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      List<GeneratedOutputFile> files) throws IOException {
    for (GeneratedOutputFile file : files) {
      validateGeneratedOutputTarget(
          canonicalRepositoryRoot,
          outputDirectory.resolve(file.fileName()));
    }

    for (GeneratedOutputFile file : files) {
      writeGeneratedFile(
          canonicalRepositoryRoot,
          outputDirectory,
          file.fileName(),
          file.content());
    }
  }

  private void writeGeneratedFile(
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      String fileName,
      String content) throws IOException {
    Path target = outputDirectory.resolve(fileName);
    validateGeneratedOutputTarget(canonicalRepositoryRoot, target);

    Path tempFile = Files.createTempFile(outputDirectory, "." + fileName + ".", ".tmp");
    boolean moved = false;
    try {
      if (!isRegularFileUnderRoot(canonicalRepositoryRoot, tempFile)) {
        throw new IOException(
            "Temporary output file is not a regular file under scan root: " + tempFile);
      }

      Files.writeString(tempFile, content, StandardCharsets.UTF_8);
      validateGeneratedOutputTarget(canonicalRepositoryRoot, target);
      moveGeneratedFile(tempFile, target);
      moved = true;

      if (!isRegularFileUnderRoot(canonicalRepositoryRoot, target)) {
        throw new IOException(
            "Output file target is not a regular file under scan root: " + target);
      }
    } finally {
      if (!moved) {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  private void validateGeneratedOutputTarget(Path canonicalRepositoryRoot, Path target)
      throws IOException {
    if (Files.isSymbolicLink(target)) {
      throw new IOException("Output file must not be a symbolic link: " + target);
    }

    if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      return;
    }

    if (!isRegularFileUnderRoot(canonicalRepositoryRoot, target)) {
      throw new IOException(
          "Output file target is not a regular file under scan root: " + target);
    }

    Long linkCount = hardLinkCount(target);
    if (linkCount != null && linkCount > 1) {
      throw new IOException("Output file must not have multiple hard links: " + target);
    }
  }

  private void moveGeneratedFile(Path tempFile, Path target) throws IOException {
    try {
      Files.move(
          tempFile,
          target,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException ex) {
      Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private Long hardLinkCount(Path target) throws IOException {
    try {
      Object value = Files.getAttribute(target, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
      if (value instanceof Number number) {
        return number.longValue();
      }
      return null;
    } catch (IllegalArgumentException | UnsupportedOperationException ex) {
      return null;
    }
  }

  private boolean isRegularFileUnderRoot(Path canonicalRepositoryRoot, Path target) {
    return ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, target)
        .filter(Files::isRegularFile)
        .isPresent();
  }

  public record Result(
      boolean generated,
      int endpointCount,
      int componentCount,
      int entityCount,
      int testCount,
      int documentCount,
      int evidenceCount) {
  }

  private record GeneratedOutputFile(String fileName, String content) {
  }

  private interface QualitySignal {
    String id();

    String moduleId();

    int moduleOrder();

    String signal();

    String status();

    String subjectKind();

    String subjectId();

    String subjectName();

    String subjectClassName();

    String subjectMemberName();
  }

  private record QualitySignals(
      String analysisStatus,
      List<TestGapSignal> testGapSignals,
      List<ChangeRiskSignal> changeRiskSignals) {
    private QualitySignals {
      testGapSignals = List.copyOf(testGapSignals);
      changeRiskSignals = List.copyOf(changeRiskSignals);
    }
  }

  private record TestGapSignal(
      String id,
      String moduleId,
      int moduleOrder,
      String signal,
      String status,
      String subjectKind,
      String subjectId,
      String subjectName,
      String subjectClassName,
      String subjectMemberName,
      String inferenceBasis,
      String confidence,
      String uncertainty,
      List<String> relatedTestIds,
      List<String> evidenceIds) implements QualitySignal {
    private TestGapSignal {
      relatedTestIds = List.copyOf(relatedTestIds);
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record ChangeRiskSignal(
      String id,
      String moduleId,
      int moduleOrder,
      String signal,
      String status,
      String subjectKind,
      String subjectId,
      String subjectName,
      String subjectClassName,
      String subjectMemberName,
      String riskBasis,
      String confidence,
      String uncertainty,
      List<String> evidenceIds) implements QualitySignal {
    private ChangeRiskSignal {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record SubjectKey(String moduleId, String className) {
  }

  private record EndpointRow(
      String moduleId,
      int moduleOrder,
      String methodLabel,
      String path,
      SpringMvcEndpointFact endpoint) {
  }

  private record ProjectLayout(
      BuildMetadata build,
      List<String> sourceRoots,
      List<String> testRoots,
      ProjectModules modules,
      Optional<EvidenceRecord> buildFileEvidence) {
    private ProjectLayout {
      sourceRoots = List.copyOf(sourceRoots);
      testRoots = List.copyOf(testRoots);
      modules = Objects.requireNonNull(modules, "modules");
    }
  }

  private record ProjectModules(String analysisStatus, List<MavenModuleItem> items) {
    private ProjectModules {
      items = List.copyOf(items);
    }
  }

  private record BuildMetadata(String system, String rootBuildFile, List<String> evidenceIds) {
    private BuildMetadata {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record ModuleAwareScan(
      List<ModuleScopedEndpointFact> endpoints,
      List<ModuleScopedWarningFact> warnings,
      List<ModuleScopedComponentFact> components,
      List<ModuleScopedSpringRepositoryFact> springRepositories,
      List<ModuleScopedSpringConfigurationClassFact> springConfigurationClasses,
      List<ModuleScopedSpringConfigurationPropertiesFact> springConfigurationProperties,
      List<ModuleScopedSpringBeanMethodFact> springBeanMethods,
      List<ModuleScopedSpringTransactionBoundaryFact> springTransactionBoundaries,
      List<ModuleScopedSpringScheduledMethodFact> springScheduledMethods,
      List<ModuleScopedSpringEventListenerFact> springEventListeners,
      List<ModuleScopedSpringMessagingListenerFact> springMessagingListeners,
      List<ModuleScopedEntityFact> entities,
      List<ModuleScopedEmbeddableFact> embeddables,
      List<ModuleScopedTestFact> tests,
      String warningAnalysisStatus,
      String componentAnalysisStatus,
      String springRepositoryAnalysisStatus,
      String springConfigurationAnalysisStatus,
      String springBehaviorAnalysisStatus,
      String springMessagingAnalysisStatus,
      String springSecurityAnalysisStatus,
      String entityAnalysisStatus,
      String testAnalysisStatus,
      List<SpringMvcEndpointEvidence> endpointEvidence,
      List<SpringComponentEvidence> componentEvidence,
      List<SpringRepositoryEvidence> springRepositoryEvidence,
      List<SpringConfigurationEvidence> springConfigurationEvidence,
      List<SpringBehaviorEvidence> springBehaviorEvidence,
      List<JpaEntityEvidence> entityEvidence,
      List<TestInventoryEvidence> testEvidence,
      List<AnalysisWarningEvidence> warningEvidence) {
    private ModuleAwareScan {
      endpoints = List.copyOf(endpoints);
      warnings = List.copyOf(warnings);
      components = List.copyOf(components);
      springRepositories = List.copyOf(springRepositories);
      springConfigurationClasses = List.copyOf(springConfigurationClasses);
      springConfigurationProperties = List.copyOf(springConfigurationProperties);
      springBeanMethods = List.copyOf(springBeanMethods);
      springTransactionBoundaries = List.copyOf(springTransactionBoundaries);
      springScheduledMethods = List.copyOf(springScheduledMethods);
      springEventListeners = List.copyOf(springEventListeners);
      springMessagingListeners = List.copyOf(springMessagingListeners);
      entities = List.copyOf(entities);
      embeddables = List.copyOf(embeddables);
      tests = List.copyOf(tests);
      endpointEvidence = List.copyOf(endpointEvidence);
      componentEvidence = List.copyOf(componentEvidence);
      springRepositoryEvidence = List.copyOf(springRepositoryEvidence);
      springConfigurationEvidence = List.copyOf(springConfigurationEvidence);
      springBehaviorEvidence = List.copyOf(springBehaviorEvidence);
      entityEvidence = List.copyOf(entityEvidence);
      testEvidence = List.copyOf(testEvidence);
      warningEvidence = List.copyOf(warningEvidence);
    }
  }

  private record ModuleScopedEndpointFact(
      String moduleId,
      int moduleOrder,
      SpringMvcEndpointFact fact) {
  }

  private record ModuleScopedComponentFact(
      String moduleId,
      int moduleOrder,
      SpringComponentFact fact) {
  }

  private record ModuleScopedSpringRepositoryFact(
      String moduleId,
      int moduleOrder,
      SpringRepositoryFact fact) {
  }

  private record ModuleScopedSpringConfigurationClassFact(
      String moduleId,
      int moduleOrder,
      SpringConfigurationClassFact fact) {
  }

  private record ModuleScopedSpringConfigurationPropertiesFact(
      String moduleId,
      int moduleOrder,
      SpringConfigurationPropertiesFact fact) {
  }

  private record ModuleScopedSpringBeanMethodFact(
      String moduleId,
      int moduleOrder,
      SpringBeanMethodFact fact) {
  }

  private record ModuleScopedSpringTransactionBoundaryFact(
      String moduleId,
      int moduleOrder,
      SpringTransactionBoundaryFact fact) {
  }

  private record ModuleScopedSpringScheduledMethodFact(
      String moduleId,
      int moduleOrder,
      SpringScheduledMethodFact fact) {
  }

  private record ModuleScopedSpringEventListenerFact(
      String moduleId,
      int moduleOrder,
      SpringEventListenerFact fact) {
  }

  private record ModuleScopedSpringMessagingListenerFact(
      String moduleId,
      int moduleOrder,
      SpringMessagingListenerFact fact) {
  }

  private record ModuleScopedEntityFact(
      String moduleId,
      int moduleOrder,
      JpaEntityFact fact) {
  }

  private record ModuleScopedEmbeddableFact(
      String moduleId,
      int moduleOrder,
      JpaEmbeddableFact fact) {
  }

  private record ModuleScopedTestFact(
      String moduleId,
      int moduleOrder,
      TestClassFact fact) {
  }

  private record ModuleScopedWarningFact(
      String id,
      String category,
      String signal,
      String moduleId,
      int moduleOrder,
      String message,
      String sourcePath,
      List<String> evidenceIds) {
    private ModuleScopedWarningFact {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record EvidenceRecord(
      String id,
      String sourceType,
      String path,
      String className,
      String methodName,
      String symbolName,
      Integer lineStart,
      Integer lineEnd,
      String excerpt,
      String confidence) {
    private EvidenceRecord {
      excerpt = EvidenceExcerpts.bounded(excerpt);
    }
  }
}
