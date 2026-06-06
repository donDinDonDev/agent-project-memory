package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

final class SpringMvcEndpointAnalyzer {
  private static final String CONTROLLER = "Controller";
  private static final String REST_CONTROLLER = "RestController";
  private static final String REQUEST_MAPPING = "RequestMapping";
  private static final String GET_MAPPING = "GetMapping";
  private static final String POST_MAPPING = "PostMapping";
  private static final String PUT_MAPPING = "PutMapping";
  private static final String PATCH_MAPPING = "PatchMapping";
  private static final String DELETE_MAPPING = "DeleteMapping";
  private static final String PATH_VARIABLE = "PathVariable";
  private static final String REQUEST_PARAM = "RequestParam";
  private static final String REQUEST_BODY = "RequestBody";
  private static final String REQUEST_METHOD = "RequestMethod";
  private static final Map<String, Set<String>> SUPPORTED_SPRING_ANNOTATIONS = Map.ofEntries(
      Map.entry(CONTROLLER, Set.of("org.springframework.stereotype.Controller")),
      Map.entry(REST_CONTROLLER, Set.of("org.springframework.web.bind.annotation.RestController")),
      Map.entry(REQUEST_MAPPING, Set.of("org.springframework.web.bind.annotation.RequestMapping")),
      Map.entry(GET_MAPPING, Set.of("org.springframework.web.bind.annotation.GetMapping")),
      Map.entry(POST_MAPPING, Set.of("org.springframework.web.bind.annotation.PostMapping")),
      Map.entry(PUT_MAPPING, Set.of("org.springframework.web.bind.annotation.PutMapping")),
      Map.entry(PATCH_MAPPING, Set.of("org.springframework.web.bind.annotation.PatchMapping")),
      Map.entry(DELETE_MAPPING, Set.of("org.springframework.web.bind.annotation.DeleteMapping")),
      Map.entry(PATH_VARIABLE, Set.of("org.springframework.web.bind.annotation.PathVariable")),
      Map.entry(REQUEST_PARAM, Set.of("org.springframework.web.bind.annotation.RequestParam")),
      Map.entry(REQUEST_BODY, Set.of("org.springframework.web.bind.annotation.RequestBody")));
  private static final Map<String, Set<String>> SUPPORTED_REQUEST_METHOD_ORIGINS = Map.of(
      REQUEST_METHOD,
      Set.of("org.springframework.web.bind.annotation.RequestMethod"));
  private static final String PATH_VARIABLE_SOURCE = "path_variable";
  private static final String REQUEST_PARAM_SOURCE = "request_param";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String CODE_SYMBOL_SOURCE_TYPE = "code_symbol";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String DIRECT_HANDLER_METHOD = "direct_handler_method";
  private static final String SOURCE_VISIBLE_INTERFACE_METHOD = "source_visible_interface_method";
  private static final String DIRECT_BINDING = "direct";
  private static final String UNIQUE_IMPLEMENTED_INTERFACE_METHOD =
      "unique_implemented_interface_method";
  private static final List<String> REQUEST_METHOD_NAMES = List.of(
      "GET",
      "HEAD",
      "POST",
      "PUT",
      "PATCH",
      "DELETE",
      "OPTIONS",
      "TRACE");

  SpringMvcEndpointAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<SpringMvcEndpointFact> endpoints = new ArrayList<>();
    List<SpringMvcEndpointEvidence> evidence = new ArrayList<>();
    List<SourceType> sourceTypes = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(
          canonicalRepositoryRoot,
          normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        sourceTypes.addAll(sourceTypes(normalizedRepositoryRoot, javaFile, sourceDeclaredTypeNames));
      }
    }

    SourceIndex sourceIndex = new SourceIndex(sourceTypes);
    for (SourceType sourceType : sourceTypes) {
      analyzeSourceType(sourceIndex, sourceType, endpoints, evidence);
    }

    return new SpringMvcEndpointAnalysis(endpoints, evidence);
  }

  private List<SourceType> sourceTypes(
      Path repositoryRoot,
      Path javaFile,
      Set<String> sourceDeclaredTypeNames) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile);
    Map<String, String> importsBySimpleName = SpringAnnotationOrigins.importsBySimpleName(compilationUnit);
    sourceDeclaredTypeNames.addAll(JavaSourceOrigins.declaredTypeNames(compilationUnit, packageName));
    List<SourceType> sourceTypes = new ArrayList<>();

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      sourceTypes.add(new SourceType(
          qualifiedClassName(packageName, type),
          packageName,
          type,
          sourcePath,
          sourceLines,
          importsBySimpleName,
          sourceDeclaredTypeNames));
    }

    return sourceTypes;
  }

  private void analyzeSourceType(
      SourceIndex sourceIndex,
      SourceType controller,
      List<SpringMvcEndpointFact> endpoints,
      List<SpringMvcEndpointEvidence> evidence) {
    ClassOrInterfaceDeclaration type = controller.declaration();
    if (type.isInterface()) {
      return;
    }

    List<AnnotationExpr> controllerAnnotations = controllerAnnotations(controller, type.getAnnotations());
    if (controllerAnnotations.isEmpty()) {
      return;
    }

    String controllerClass = controller.className();
    List<SpringMvcEndpointEvidence> controllerAnnotationEvidence = controllerAnnotations.stream()
        .map(annotation -> mappingEvidence(
            controller.sourcePath(),
            controllerClass,
            null,
            annotation,
            controller.sourceLines()))
        .toList();
    Optional<AnnotationExpr> requestMapping = findAnnotation(
        controller,
        type.getAnnotations(),
        REQUEST_MAPPING);
    ExtractedPaths classPathExtraction = requestMapping
        .map(this::literalPathValues)
        .orElseGet(ExtractedPaths::notDeclared);
    if (classPathExtraction.isDeclaredButUnsupported()) {
      return;
    }
    List<String> classPaths = classPathExtraction.pathsOrDefaultRoot();
    SpringMvcEndpointEvidence requestMappingEvidence = requestMapping
        .map(annotation -> mappingEvidence(
            controller.sourcePath(),
            controllerClass,
            null,
            annotation,
            controller.sourceLines()))
        .orElse(null);

    for (MethodDeclaration method : type.getMethods()) {
      Optional<AnnotationExpr> methodMapping = findMethodMapping(controller, method.getAnnotations());
      if (methodMapping.isPresent()) {
        addDirectEndpoint(
            controller,
            method,
            methodMapping.orElseThrow(),
            classPaths,
            requestMappingEvidence,
            controllerAnnotationEvidence,
            endpoints,
            evidence);
        continue;
      }

      addInterfaceEndpointIfUnique(
          sourceIndex,
          controller,
          method,
          classPaths,
          requestMappingEvidence,
          controllerAnnotationEvidence,
          endpoints,
          evidence);
    }
  }

  private void addDirectEndpoint(
      SourceType controller,
      MethodDeclaration method,
      AnnotationExpr methodMappingAnnotation,
      List<String> classPaths,
      SpringMvcEndpointEvidence requestMappingEvidence,
      List<SpringMvcEndpointEvidence> controllerAnnotationEvidence,
      List<SpringMvcEndpointFact> endpoints,
      List<SpringMvcEndpointEvidence> evidence) {
    ExtractedPaths methodPathExtraction = literalPathValues(methodMappingAnnotation);
    if (methodPathExtraction.isDeclaredButUnsupported()) {
      return;
    }
    ExtractedHttpMethods httpMethods = httpMethods(controller, methodMappingAnnotation);
    ExtractedRequestMetadata requestMetadata = requestMetadata(controller, method);
    SpringMvcEndpointEvidence methodMappingEvidence = mappingEvidence(
        controller.sourcePath(),
        controller.className(),
        method.getNameAsString(),
        methodMappingAnnotation,
        controller.sourceLines());
    controllerAnnotationEvidence.forEach(
        controllerEvidence -> addEvidenceIfAbsent(evidence, controllerEvidence));
    addEvidenceIfAbsent(evidence, requestMappingEvidence);
    addEvidenceIfAbsent(evidence, methodMappingEvidence);
    requestMetadata.evidence().forEach(metadataEvidence -> addEvidenceIfAbsent(evidence, metadataEvidence));

    List<String> evidenceIds = new ArrayList<>();
    controllerAnnotationEvidence.stream()
        .map(SpringMvcEndpointEvidence::id)
        .forEach(evidenceIds::add);
    if (requestMappingEvidence != null) {
      evidenceIds.add(requestMappingEvidence.id());
    }
    evidenceIds.add(methodMappingEvidence.id());
    requestMetadata.requestParameters().stream()
        .flatMap(parameter -> parameter.evidenceIds().stream())
        .forEach(evidenceIds::add);
    evidenceIds.addAll(requestMetadata.requestBodyEvidenceIds());

    List<String> mappingSourceEvidenceIds = new ArrayList<>();
    if (requestMappingEvidence != null) {
      mappingSourceEvidenceIds.add(requestMappingEvidence.id());
    }
    mappingSourceEvidenceIds.add(methodMappingEvidence.id());

    SpringMvcEndpointMappingSource mappingSource = new SpringMvcEndpointMappingSource(
        DIRECT_HANDLER_METHOD,
        controller.className(),
        method.getNameAsString(),
        DIRECT_BINDING,
        null,
        mappingSourceEvidenceIds);

    endpoints.add(new SpringMvcEndpointFact(
        controller.className(),
        method.getNameAsString(),
        httpMethods.methods(),
        httpMethods.semantics(),
        combinePaths(classPaths, methodPathExtraction.pathsOrDefaultRoot()),
        requestMetadata.requestParameters(),
        requestMetadata.requestBodyType(),
        requestMetadata.requestBodyEvidenceIds(),
        method.getType().asString(),
        mappingSource,
        evidenceIds));
  }

  private void addInterfaceEndpointIfUnique(
      SourceIndex sourceIndex,
      SourceType controller,
      MethodDeclaration controllerMethod,
      List<String> controllerClassPaths,
      SpringMvcEndpointEvidence controllerRequestMappingEvidence,
      List<SpringMvcEndpointEvidence> controllerAnnotationEvidence,
      List<SpringMvcEndpointFact> endpoints,
      List<SpringMvcEndpointEvidence> evidence) {
    List<InterfaceMappingCandidate> candidates = interfaceMappingCandidates(
        sourceIndex,
        controller,
        controllerMethod);
    if (candidates.size() != 1) {
      return;
    }

    InterfaceMappingCandidate candidate = candidates.get(0);
    ExtractedHttpMethods httpMethods = httpMethods(
        candidate.interfaceType(),
        candidate.methodMappingAnnotation());
    ExtractedRequestMetadata requestMetadata = requestMetadata(
        candidate.interfaceType(),
        candidate.interfaceMethod());
    SpringMvcEndpointEvidence interfaceMethodMappingEvidence = mappingEvidence(
        candidate.interfaceType().sourcePath(),
        candidate.interfaceType().className(),
        candidate.interfaceMethod().getNameAsString(),
        candidate.methodMappingAnnotation(),
        candidate.interfaceType().sourceLines());
    SpringMvcEndpointEvidence interfaceRequestMappingEvidence = candidate.interfaceRequestMapping()
        .map(annotation -> mappingEvidence(
            candidate.interfaceType().sourcePath(),
            candidate.interfaceType().className(),
            null,
            annotation,
            candidate.interfaceType().sourceLines()))
        .orElse(null);
    List<SpringMvcEndpointEvidence> bindingEvidence = List.of(
        codeSymbolEvidence(
            candidate.interfaceType().sourcePath(),
            candidate.interfaceType().className(),
            candidate.interfaceMethod(),
            candidate.interfaceType().sourceLines()),
        codeSymbolEvidence(
            controller.sourcePath(),
            controller.className(),
            controller.declaration(),
            controller.sourceLines()),
        codeSymbolEvidence(
            controller.sourcePath(),
            controller.className(),
            controllerMethod,
            controller.sourceLines()));

    controllerAnnotationEvidence.forEach(
        controllerEvidence -> addEvidenceIfAbsent(evidence, controllerEvidence));
    addEvidenceIfAbsent(evidence, controllerRequestMappingEvidence);
    addEvidenceIfAbsent(evidence, interfaceRequestMappingEvidence);
    addEvidenceIfAbsent(evidence, interfaceMethodMappingEvidence);
    requestMetadata.evidence().forEach(metadataEvidence -> addEvidenceIfAbsent(evidence, metadataEvidence));
    bindingEvidence.forEach(binding -> addEvidenceIfAbsent(evidence, binding));

    List<String> evidenceIds = new ArrayList<>();
    controllerAnnotationEvidence.stream()
        .map(SpringMvcEndpointEvidence::id)
        .forEach(evidenceIds::add);
    if (controllerRequestMappingEvidence != null) {
      evidenceIds.add(controllerRequestMappingEvidence.id());
    }
    if (interfaceRequestMappingEvidence != null) {
      evidenceIds.add(interfaceRequestMappingEvidence.id());
    }
    evidenceIds.add(interfaceMethodMappingEvidence.id());
    requestMetadata.requestParameters().stream()
        .flatMap(parameter -> parameter.evidenceIds().stream())
        .forEach(evidenceIds::add);
    evidenceIds.addAll(requestMetadata.requestBodyEvidenceIds());
    bindingEvidence.stream()
        .map(SpringMvcEndpointEvidence::id)
        .forEach(evidenceIds::add);

    List<String> mappingSourceEvidenceIds = new ArrayList<>();
    if (interfaceRequestMappingEvidence != null) {
      mappingSourceEvidenceIds.add(interfaceRequestMappingEvidence.id());
    }
    mappingSourceEvidenceIds.add(interfaceMethodMappingEvidence.id());
    if (controllerRequestMappingEvidence != null) {
      mappingSourceEvidenceIds.add(controllerRequestMappingEvidence.id());
    }
    bindingEvidence.stream()
        .map(SpringMvcEndpointEvidence::id)
        .forEach(mappingSourceEvidenceIds::add);

    SpringMvcEndpointMappingSource mappingSource = new SpringMvcEndpointMappingSource(
        SOURCE_VISIBLE_INTERFACE_METHOD,
        candidate.interfaceType().className(),
        candidate.interfaceMethod().getNameAsString(),
        UNIQUE_IMPLEMENTED_INTERFACE_METHOD,
        null,
        mappingSourceEvidenceIds);

    endpoints.add(new SpringMvcEndpointFact(
        controller.className(),
        controllerMethod.getNameAsString(),
        httpMethods.methods(),
        httpMethods.semantics(),
        combinePaths(
            combinePaths(controllerClassPaths, candidate.interfaceClassPaths()),
            candidate.methodPathExtraction().pathsOrDefaultRoot()),
        requestMetadata.requestParameters(),
        requestMetadata.requestBodyType(),
        requestMetadata.requestBodyEvidenceIds(),
        controllerMethod.getType().asString(),
        mappingSource,
        evidenceIds));
  }

  private List<InterfaceMappingCandidate> interfaceMappingCandidates(
      SourceIndex sourceIndex,
      SourceType controller,
      MethodDeclaration controllerMethod) {
    List<InterfaceMappingCandidate> candidates = new ArrayList<>();
    for (SourceType interfaceType : sourceIndex.implementedInterfaces(controller)) {
      Optional<AnnotationExpr> interfaceRequestMapping = findAnnotation(
          interfaceType,
          interfaceType.declaration().getAnnotations(),
          REQUEST_MAPPING);
      ExtractedPaths interfaceClassPathExtraction = interfaceRequestMapping
          .map(this::literalPathValues)
          .orElseGet(ExtractedPaths::notDeclared);
      if (interfaceClassPathExtraction.isDeclaredButUnsupported()) {
        continue;
      }

      for (MethodDeclaration interfaceMethod : interfaceType.declaration().getMethods()) {
        if (!interfaceMethod.getNameAsString().equals(controllerMethod.getNameAsString())) {
          continue;
        }
        if (!hasCompatibleParameterShapes(interfaceMethod, controllerMethod)) {
          continue;
        }
        Optional<AnnotationExpr> methodMapping = findMethodMapping(
            interfaceType,
            interfaceMethod.getAnnotations());
        if (methodMapping.isEmpty()) {
          continue;
        }
        ExtractedPaths methodPathExtraction = literalPathValues(methodMapping.orElseThrow());
        if (methodPathExtraction.isDeclaredButUnsupported()) {
          continue;
        }

        candidates.add(new InterfaceMappingCandidate(
            interfaceType,
            interfaceMethod,
            interfaceRequestMapping,
            interfaceClassPathExtraction.pathsOrDefaultRoot(),
            methodMapping.orElseThrow(),
            methodPathExtraction));
      }
    }
    return candidates;
  }

  private boolean hasCompatibleParameterShapes(
      MethodDeclaration interfaceMethod,
      MethodDeclaration controllerMethod) {
    if (interfaceMethod.getParameters().size() != controllerMethod.getParameters().size()) {
      return false;
    }

    for (int index = 0; index < interfaceMethod.getParameters().size(); index++) {
      String interfaceType = interfaceMethod.getParameter(index).getType().asString();
      String controllerType = controllerMethod.getParameter(index).getType().asString();
      if (!compatibleTypeShape(interfaceType, controllerType)) {
        return false;
      }
    }

    return true;
  }

  private boolean compatibleTypeShape(String left, String right) {
    String normalizedLeft = normalizedTypeShape(left);
    String normalizedRight = normalizedTypeShape(right);
    return normalizedLeft.equals(normalizedRight)
        || simpleTypeShape(normalizedLeft).equals(simpleTypeShape(normalizedRight));
  }

  private String normalizedTypeShape(String type) {
    return type.replaceAll("\\s+", "");
  }

  private String simpleTypeShape(String type) {
    return type.replaceAll("([a-z_][\\w$]*\\.)+([A-Z][\\w$]*)", "$2");
  }

  private Path normalizeSourceRoot(Path repositoryRoot, Path sourceRoot) {
    Objects.requireNonNull(sourceRoot, "sourceRoot");
    if (sourceRoot.isAbsolute()) {
      return sourceRoot.toAbsolutePath().normalize();
    }
    return repositoryRoot.resolve(sourceRoot).normalize();
  }

  private List<Path> javaFiles(Path canonicalRepositoryRoot, Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, path)
              && path.getFileName().toString().endsWith(".java"))
          .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
          .toList();
    }
  }

  private List<AnnotationExpr> controllerAnnotations(
      SourceType sourceType,
      List<AnnotationExpr> annotations) {
    return annotations.stream()
        .filter(annotation -> {
          Optional<String> simpleName = supportedSpringAnnotationName(sourceType, annotation);
          return simpleName
              .filter(name -> CONTROLLER.equals(name) || REST_CONTROLLER.equals(name))
              .isPresent();
        })
        .toList();
  }

  private Optional<AnnotationExpr> findMethodMapping(
      SourceType sourceType,
      List<AnnotationExpr> annotations) {
    return annotations.stream()
        .filter(annotation -> supportedSpringAnnotationName(sourceType, annotation)
            .filter(this::isSupportedMethodMapping)
            .isPresent())
        .findFirst();
  }

  private boolean isSupportedMethodMapping(String simpleName) {
    return REQUEST_MAPPING.equals(simpleName)
        || GET_MAPPING.equals(simpleName)
        || POST_MAPPING.equals(simpleName)
        || PUT_MAPPING.equals(simpleName)
        || PATCH_MAPPING.equals(simpleName)
        || DELETE_MAPPING.equals(simpleName);
  }

  private Optional<AnnotationExpr> findAnnotation(
      SourceType sourceType,
      List<AnnotationExpr> annotations,
      String simpleName) {
    return annotations.stream()
        .filter(annotation -> supportedSpringAnnotationName(sourceType, annotation)
            .filter(simpleName::equals)
            .isPresent())
        .findFirst();
  }

  private Optional<String> supportedSpringAnnotationName(
      SourceType sourceType,
      AnnotationExpr annotation) {
    return SpringAnnotationOrigins.supportedSimpleName(
        annotation,
        sourceType.importsBySimpleName(),
        SUPPORTED_SPRING_ANNOTATIONS,
        sourceType.sourceDeclaredTypeNames());
  }

  private ExtractedRequestMetadata requestMetadata(
      SourceType sourceType,
      MethodDeclaration method) {
    List<SpringMvcRequestParameterFact> requestParameters = new ArrayList<>();
    List<String> requestBodyEvidenceIds = new ArrayList<>();
    List<SpringMvcEndpointEvidence> evidence = new ArrayList<>();
    String requestBodyType = null;

    for (int index = 0; index < method.getParameters().size(); index++) {
      Parameter parameter = method.getParameter(index);
      Optional<AnnotationExpr> pathVariable = findAnnotation(
          sourceType,
          parameter.getAnnotations(),
          PATH_VARIABLE);
      if (pathVariable.isPresent()) {
        addRequestParameterFact(
            sourceType.sourcePath(),
            sourceType.className(),
            method,
            sourceType.sourceLines(),
            requestParameters,
            evidence,
            parameter,
            pathVariable.orElseThrow(),
            index,
            PATH_VARIABLE_SOURCE);
      }

      Optional<AnnotationExpr> requestParam = findAnnotation(
          sourceType,
          parameter.getAnnotations(),
          REQUEST_PARAM);
      if (requestParam.isPresent()) {
        addRequestParameterFact(
            sourceType.sourcePath(),
            sourceType.className(),
            method,
            sourceType.sourceLines(),
            requestParameters,
            evidence,
            parameter,
            requestParam.orElseThrow(),
            index,
            REQUEST_PARAM_SOURCE);
      }

      Optional<AnnotationExpr> requestBody = findAnnotation(
          sourceType,
          parameter.getAnnotations(),
          REQUEST_BODY);
      if (requestBody.isPresent() && requestBodyType == null) {
        SpringMvcEndpointEvidence requestBodyEvidence = parameterAnnotationEvidence(
            sourceType.sourcePath(),
            sourceType.className(),
            method.getNameAsString(),
            parameter,
            requestBody.orElseThrow(),
            sourceType.sourceLines(),
            index);
        evidence.add(requestBodyEvidence);
        requestBodyType = parameter.getType().asString();
        requestBodyEvidenceIds.add(requestBodyEvidence.id());
      }
    }

    return new ExtractedRequestMetadata(
        requestParameters,
        requestBodyType,
        requestBodyEvidenceIds,
        evidence);
  }

  private void addRequestParameterFact(
      String sourcePath,
      String controllerClass,
      MethodDeclaration method,
      List<String> sourceLines,
      List<SpringMvcRequestParameterFact> requestParameters,
      List<SpringMvcEndpointEvidence> evidence,
      Parameter parameter,
      AnnotationExpr annotation,
      int parameterIndex,
      String source) {
    Optional<String> metadataName = requestMetadataName(annotation, parameter);
    if (metadataName.isEmpty()) {
      return;
    }

    SpringMvcEndpointEvidence parameterEvidence = parameterAnnotationEvidence(
        sourcePath,
        controllerClass,
        method.getNameAsString(),
        parameter,
        annotation,
        sourceLines,
        parameterIndex);
    evidence.add(parameterEvidence);
    requestParameters.add(new SpringMvcRequestParameterFact(
        metadataName.orElseThrow(),
        source,
        parameter.getType().asString(),
        List.of(parameterEvidence.id())));
  }

  private Optional<String> requestMetadataName(AnnotationExpr annotation, Parameter parameter) {
    if (annotation.isSingleMemberAnnotationExpr()) {
      return literalStringValue(annotation.asSingleMemberAnnotationExpr().getMemberValue());
    }

    if (annotation.isNormalAnnotationExpr()) {
      for (var pair : annotation.asNormalAnnotationExpr().getPairs()) {
        String memberName = pair.getNameAsString();
        if ("value".equals(memberName) || "name".equals(memberName)) {
          return literalStringValue(pair.getValue());
        }
      }
    }

    return Optional.of(parameter.getNameAsString());
  }

  private Optional<String> literalStringValue(Expression expression) {
    if (expression.isStringLiteralExpr()) {
      return Optional.of(expression.asStringLiteralExpr().asString());
    }
    return Optional.empty();
  }

  private String simpleAnnotationName(AnnotationExpr annotation) {
    return SpringAnnotationOrigins.simpleAnnotationName(annotation);
  }

  private ExtractedHttpMethods httpMethods(SourceType sourceType, AnnotationExpr annotation) {
    return switch (simpleAnnotationName(annotation)) {
      case GET_MAPPING -> ExtractedHttpMethods.declared(List.of("GET"));
      case POST_MAPPING -> ExtractedHttpMethods.declared(List.of("POST"));
      case PUT_MAPPING -> ExtractedHttpMethods.declared(List.of("PUT"));
      case PATCH_MAPPING -> ExtractedHttpMethods.declared(List.of("PATCH"));
      case DELETE_MAPPING -> ExtractedHttpMethods.declared(List.of("DELETE"));
      case REQUEST_MAPPING -> requestMappingHttpMethods(sourceType, annotation);
      default -> ExtractedHttpMethods.unsupported();
    };
  }

  private ExtractedHttpMethods requestMappingHttpMethods(
      SourceType sourceType,
      AnnotationExpr annotation) {
    if (!annotation.isNormalAnnotationExpr()) {
      return ExtractedHttpMethods.notDeclared();
    }

    return annotation.asNormalAnnotationExpr().getPairs().stream()
        .filter(pair -> "method".equals(pair.getNameAsString()))
        .findFirst()
        .map(pair -> requestMethodValues(sourceType, pair.getValue()))
        .orElseGet(ExtractedHttpMethods::notDeclared);
  }

  private ExtractedHttpMethods requestMethodValues(SourceType sourceType, Expression expression) {
    if (expression.isArrayInitializerExpr()) {
      ArrayInitializerExpr array = expression.asArrayInitializerExpr();
      LinkedHashSet<String> methods = new LinkedHashSet<>();
      for (Expression value : array.getValues()) {
        Optional<String> requestMethod = requestMethodName(sourceType, value);
        if (requestMethod.isEmpty()) {
          return ExtractedHttpMethods.unsupported();
        }
        methods.add(requestMethod.orElseThrow());
      }
      return ExtractedHttpMethods.declared(List.copyOf(methods));
    }

    return requestMethodName(sourceType, expression)
        .map(method -> ExtractedHttpMethods.declared(List.of(method)))
        .orElseGet(ExtractedHttpMethods::unsupported);
  }

  private Optional<String> requestMethodName(SourceType sourceType, Expression expression) {
    if (expression.isFieldAccessExpr()) {
      String scope = expression.asFieldAccessExpr().getScope().toString();
      String methodName = expression.asFieldAccessExpr().getNameAsString();
      boolean supportedRequestMethodScope = JavaSourceOrigins.supportedTypeSimpleName(
              scope,
              sourceType.importsBySimpleName(),
              SUPPORTED_REQUEST_METHOD_ORIGINS,
              sourceType.sourceDeclaredTypeNames())
          .filter(REQUEST_METHOD::equals)
          .isPresent();
      if (supportedRequestMethodScope && REQUEST_METHOD_NAMES.contains(methodName)) {
        return Optional.of(methodName);
      }
    }

    return Optional.empty();
  }

  private ExtractedPaths literalPathValues(AnnotationExpr annotation) {
    if (annotation.isSingleMemberAnnotationExpr()) {
      return literalPathValues(annotation.asSingleMemberAnnotationExpr().getMemberValue());
    }

    if (annotation.isNormalAnnotationExpr()) {
      return annotation.asNormalAnnotationExpr().getPairs().stream()
          .filter(pair -> "value".equals(pair.getNameAsString()) || "path".equals(pair.getNameAsString()))
          .findFirst()
          .map(pair -> literalPathValues(pair.getValue()))
          .orElseGet(ExtractedPaths::notDeclared);
    }

    return ExtractedPaths.notDeclared();
  }

  private ExtractedPaths literalPathValues(Expression expression) {
    if (expression.isStringLiteralExpr()) {
      return ExtractedPaths.declared(List.of(expression.asStringLiteralExpr().asString()));
    }

    if (expression.isArrayInitializerExpr()) {
      ArrayInitializerExpr array = expression.asArrayInitializerExpr();
      List<String> paths = new ArrayList<>();
      for (Expression value : array.getValues()) {
        if (!value.isStringLiteralExpr()) {
          return ExtractedPaths.unsupported();
        }
        paths.add(value.asStringLiteralExpr().asString());
      }
      return ExtractedPaths.declared(paths);
    }

    return ExtractedPaths.unsupported();
  }

  private void addEvidenceIfAbsent(
      List<SpringMvcEndpointEvidence> evidence,
      SpringMvcEndpointEvidence candidate) {
    if (candidate == null) {
      return;
    }

    boolean alreadyAdded = evidence.stream()
        .anyMatch(existing -> existing.id().equals(candidate.id()));
    if (!alreadyAdded) {
      evidence.add(candidate);
    }
  }

  private SpringMvcEndpointEvidence mappingEvidence(
      String sourcePath,
      String className,
      String methodName,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    String annotationSymbol = "@" + simpleAnnotationName(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);

    return new SpringMvcEndpointEvidence(
        evidenceId(sourcePath, className, methodName, annotationSymbol, lineStart, lineEnd, null),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        methodName,
        annotationSymbol,
        lineStart,
        lineEnd,
        excerpt(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private SpringMvcEndpointEvidence parameterAnnotationEvidence(
      String sourcePath,
      String className,
      String methodName,
      Parameter parameter,
      AnnotationExpr annotation,
      List<String> sourceLines,
      int parameterIndex) {
    String annotationSymbol = "@" + simpleAnnotationName(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    String discriminator = "parameter:" + parameterIndex + ":" + parameter.getNameAsString();

    return new SpringMvcEndpointEvidence(
        evidenceId(sourcePath, className, methodName, annotationSymbol, lineStart, lineEnd, discriminator),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        methodName,
        annotationSymbol,
        lineStart,
        lineEnd,
        excerpt(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private SpringMvcEndpointEvidence codeSymbolEvidence(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      List<String> sourceLines) {
    Integer lineStart = classDeclarationLine(type);
    Integer lineEnd = lineStart;

    return new SpringMvcEndpointEvidence(
        evidenceId(sourcePath, className, null, CODE_SYMBOL_SOURCE_TYPE, lineStart, lineEnd, null),
        CODE_SYMBOL_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        className,
        lineStart,
        lineEnd,
        singleLineExcerpt(type, sourceLines, lineStart),
        HIGH_CONFIDENCE);
  }

  private SpringMvcEndpointEvidence codeSymbolEvidence(
      String sourcePath,
      String className,
      MethodDeclaration method,
      List<String> sourceLines) {
    String methodName = method.getNameAsString();
    Integer lineStart = methodDeclarationLine(method);
    Integer lineEnd = lineStart;

    return new SpringMvcEndpointEvidence(
        evidenceId(sourcePath, className, methodName, CODE_SYMBOL_SOURCE_TYPE, lineStart, lineEnd, null),
        CODE_SYMBOL_SOURCE_TYPE,
        sourcePath,
        className,
        methodName,
        className + "#" + methodName,
        lineStart,
        lineEnd,
        singleLineExcerpt(method, sourceLines, lineStart),
        HIGH_CONFIDENCE);
  }

  private String evidenceId(
      String sourcePath,
      String className,
      String methodName,
      String annotationSymbol,
      Integer lineStart,
      Integer lineEnd,
      String discriminator) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    String symbolOwner = methodName == null ? className : className + "#" + methodName;
    String id = "ev:" + sourcePath + ":" + lineRange + ":" + symbolOwner + ":" + annotationSymbol;
    if (discriminator == null || discriminator.isBlank()) {
      return id;
    }
    return id + ":" + discriminator;
  }

  private String excerpt(AnnotationExpr annotation, List<String> sourceLines) {
    return EvidenceExcerpts.sourceRange(annotation, sourceLines);
  }

  private String singleLineExcerpt(Node node, List<String> sourceLines, Integer preferredLine) {
    return EvidenceExcerpts.singleLine(node, sourceLines, preferredLine);
  }

  private Integer classDeclarationLine(ClassOrInterfaceDeclaration type) {
    return type.getName().getRange()
        .map(range -> range.begin.line)
        .orElseGet(() -> type.getRange().map(range -> range.begin.line).orElse(null));
  }

  private Integer methodDeclarationLine(MethodDeclaration method) {
    return method.getName().getRange()
        .map(range -> range.begin.line)
        .orElseGet(() -> method.getRange().map(range -> range.begin.line).orElse(null));
  }

  private String repositoryRelativePath(Path repositoryRoot, Path javaFile) {
    Path relativePath = repositoryRoot.relativize(javaFile.toAbsolutePath().normalize());
    return relativePath.toString().replace(javaFile.getFileSystem().getSeparator(), "/");
  }

  private String qualifiedClassName(String packageName, String className) {
    if (packageName.isBlank()) {
      return className;
    }
    return packageName + "." + className;
  }

  private String qualifiedClassName(String packageName, ClassOrInterfaceDeclaration type) {
    return type.getFullyQualifiedName()
        .orElseGet(() -> qualifiedClassName(packageName, type.getNameAsString()));
  }

  private List<String> combinePaths(List<String> classPaths, List<String> methodPaths) {
    LinkedHashSet<String> paths = new LinkedHashSet<>();
    for (String classPath : classPaths) {
      for (String methodPath : methodPaths) {
        paths.add(combinePath(classPath, methodPath));
      }
    }
    return List.copyOf(paths);
  }

  private String combinePath(String classPath, String methodPath) {
    String normalizedClassPath = normalizePath(classPath);
    String normalizedMethodPath = normalizePath(methodPath);

    if (normalizedClassPath.isEmpty() && normalizedMethodPath.isEmpty()) {
      return "/";
    }

    if (normalizedClassPath.isEmpty()) {
      return ensureLeadingSlash(normalizedMethodPath);
    }

    if (normalizedMethodPath.isEmpty() || "/".equals(normalizedMethodPath)) {
      return ensureLeadingSlash(stripTrailingSlash(normalizedClassPath));
    }

    String classSegment = ensureLeadingSlash(stripTrailingSlash(normalizedClassPath));
    String methodSegment = ensureLeadingSlash(normalizedMethodPath);
    if ("/".equals(classSegment)) {
      return methodSegment;
    }
    return classSegment + methodSegment;
  }

  private String normalizePath(String path) {
    if (path == null) {
      return "";
    }
    return path.trim();
  }

  private String ensureLeadingSlash(String path) {
    if (path.isEmpty() || path.startsWith("/")) {
      return path;
    }
    return "/" + path;
  }

  private String stripTrailingSlash(String path) {
    while (path.length() > 1 && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  private record ExtractedHttpMethods(
      SpringMvcHttpMethodSemantics semantics,
      List<String> methods) {
    private ExtractedHttpMethods {
      methods = List.copyOf(methods);
    }

    private static ExtractedHttpMethods declared(List<String> methods) {
      return new ExtractedHttpMethods(SpringMvcHttpMethodSemantics.DECLARED, methods);
    }

    private static ExtractedHttpMethods notDeclared() {
      return new ExtractedHttpMethods(SpringMvcHttpMethodSemantics.NOT_DECLARED, List.of());
    }

    private static ExtractedHttpMethods unsupported() {
      return new ExtractedHttpMethods(SpringMvcHttpMethodSemantics.UNSUPPORTED, List.of());
    }
  }

  private record ExtractedRequestMetadata(
      List<SpringMvcRequestParameterFact> requestParameters,
      String requestBodyType,
      List<String> requestBodyEvidenceIds,
      List<SpringMvcEndpointEvidence> evidence) {
    private ExtractedRequestMetadata {
      requestParameters = List.copyOf(requestParameters);
      requestBodyEvidenceIds = List.copyOf(requestBodyEvidenceIds);
      evidence = List.copyOf(evidence);
    }
  }

  private record ExtractedPaths(boolean pathDeclared, List<String> paths) {
    private ExtractedPaths {
      paths = List.copyOf(paths);
    }

    private static ExtractedPaths notDeclared() {
      return new ExtractedPaths(false, List.of());
    }

    private static ExtractedPaths unsupported() {
      return new ExtractedPaths(true, List.of());
    }

    private static ExtractedPaths declared(List<String> paths) {
      return new ExtractedPaths(true, paths);
    }

    private boolean isDeclaredButUnsupported() {
      return pathDeclared && paths.isEmpty();
    }

    private List<String> pathsOrDefaultRoot() {
      if (paths.isEmpty()) {
        return List.of("");
      }
      return paths;
    }
  }

  private record SourceType(
      String className,
      String packageName,
      ClassOrInterfaceDeclaration declaration,
      String sourcePath,
      List<String> sourceLines,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    private SourceType {
      sourceLines = List.copyOf(sourceLines);
      importsBySimpleName = Map.copyOf(importsBySimpleName);
    }
  }

  private record InterfaceMappingCandidate(
      SourceType interfaceType,
      MethodDeclaration interfaceMethod,
      Optional<AnnotationExpr> interfaceRequestMapping,
      List<String> interfaceClassPaths,
      AnnotationExpr methodMappingAnnotation,
      ExtractedPaths methodPathExtraction) {
    private InterfaceMappingCandidate {
      interfaceClassPaths = List.copyOf(interfaceClassPaths);
    }
  }

  private final class SourceIndex {
    private final Map<String, SourceType> byQualifiedName;

    private SourceIndex(List<SourceType> sourceTypes) {
      Map<String, SourceType> qualified = new LinkedHashMap<>();
      for (SourceType sourceType : sourceTypes) {
        qualified.putIfAbsent(sourceType.className(), sourceType);
      }
      this.byQualifiedName = Map.copyOf(qualified);
    }

    private List<SourceType> implementedInterfaces(SourceType controller) {
      Map<String, SourceType> interfaces = new LinkedHashMap<>();
      for (ClassOrInterfaceType implementedType : controller.declaration().getImplementedTypes()) {
        resolveImplementedType(controller, implementedType)
            .filter(sourceType -> sourceType.declaration().isInterface())
            .ifPresent(sourceType -> interfaces.putIfAbsent(sourceType.className(), sourceType));
      }
      return List.copyOf(interfaces.values());
    }

    private Optional<SourceType> resolveImplementedType(
        SourceType controller,
        ClassOrInterfaceType implementedType) {
      String nameWithScope = implementedType.getNameWithScope();
      if (nameWithScope.contains(".")) {
        SourceType exact = byQualifiedName.get(nameWithScope);
        if (exact != null) {
          return Optional.of(exact);
        }
        return Optional.empty();
      }

      String simpleImplementedName = implementedType.getNameAsString();
      String importedName = controller.importsBySimpleName().get(simpleImplementedName);
      if (importedName != null) {
        SourceType imported = byQualifiedName.get(importedName);
        if (imported != null) {
          return Optional.of(imported);
        }
        return Optional.empty();
      }

      String samePackageName = qualifiedClassName(controller.packageName(), simpleImplementedName);
      SourceType samePackage = byQualifiedName.get(samePackageName);
      if (samePackage != null) {
        return Optional.of(samePackage);
      }

      return Optional.empty();
    }
  }
}
