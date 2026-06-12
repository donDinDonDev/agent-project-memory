package io.github.dondindondev.agentprojectmemory.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class JavaSourceOrigins {
  private static final String INCOMPLETE_SOURCE_INDEX_MARKER =
      "__agent_project_memory_incomplete_java_source_index__";

  private JavaSourceOrigins() {
  }

  public static Map<String, String> singleTypeImportsBySimpleName(CompilationUnit compilationUnit) {
    Map<String, String> imports = new LinkedHashMap<>();
    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
      if (importDeclaration.isStatic() || importDeclaration.isAsterisk()) {
        continue;
      }
      String importName = importDeclaration.getNameAsString();
      imports.putIfAbsent(simpleName(importName), importName);
    }
    return imports;
  }

  public static Set<String> wildcardImportPackages(CompilationUnit compilationUnit) {
    Set<String> imports = new LinkedHashSet<>();
    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
      if (importDeclaration.isStatic() || !importDeclaration.isAsterisk()) {
        continue;
      }
      imports.add(importDeclaration.getNameAsString());
    }
    return imports;
  }

  public static Set<String> declaredTypeNames(
      CompilationUnit compilationUnit,
      String packageName) {
    Set<String> declaredTypeNames = new LinkedHashSet<>();
    for (TypeDeclaration<?> type : compilationUnit.findAll(TypeDeclaration.class)) {
      type.getFullyQualifiedName()
          .ifPresentOrElse(
              declaredTypeNames::add,
              () -> declaredTypeNames.add(qualifiedTypeName(packageName, type.getNameAsString())));
    }
    return declaredTypeNames;
  }

  public static Optional<String> supportedAnnotationSimpleName(
      AnnotationExpr annotation,
      Map<String, String> singleTypeImportsBySimpleName,
      Map<String, ? extends Collection<String>> supportedOrigins,
      Set<String> sourceDeclaredTypeNames) {
    return supportedTypeSimpleName(
        annotation.getNameAsString(),
        singleTypeImportsBySimpleName,
        supportedOrigins,
        sourceDeclaredTypeNames);
  }

  public static Optional<String> supportedTypeSimpleName(
      String referenceName,
      Map<String, String> singleTypeImportsBySimpleName,
      Map<String, ? extends Collection<String>> supportedOrigins,
      Set<String> sourceDeclaredTypeNames) {
    if (sourceDeclaredTypeNames.contains(INCOMPLETE_SOURCE_INDEX_MARKER)) {
      return Optional.empty();
    }

    String simpleName = simpleName(referenceName);
    Collection<String> supportedQualifiedNames = supportedOrigins.get(simpleName);
    if (supportedQualifiedNames == null) {
      return Optional.empty();
    }

    if (referenceName.contains(".")) {
      if (isSupportedExternalOrigin(referenceName, supportedQualifiedNames, sourceDeclaredTypeNames)) {
        return Optional.of(simpleName);
      }
      return Optional.empty();
    }

    String importedQualifiedName = singleTypeImportsBySimpleName.get(simpleName);
    if (isSupportedExternalOrigin(importedQualifiedName, supportedQualifiedNames, sourceDeclaredTypeNames)) {
      return Optional.of(simpleName);
    }
    return Optional.empty();
  }

  public static String simpleAnnotationName(AnnotationExpr annotation) {
    return simpleName(annotation.getNameAsString());
  }

  public static void markIncompleteSourceIndexIfNeeded(Set<String> sourceDeclaredTypeNames) {
    if (JavaSourceParser.hasSkippedSources()) {
      sourceDeclaredTypeNames.add(INCOMPLETE_SOURCE_INDEX_MARKER);
    }
  }

  public static boolean isIncompleteSourceIndex(Set<String> sourceDeclaredTypeNames) {
    return sourceDeclaredTypeNames.contains(INCOMPLETE_SOURCE_INDEX_MARKER);
  }

  public static String simpleName(String name) {
    int lastDot = name.lastIndexOf('.');
    if (lastDot >= 0) {
      return name.substring(lastDot + 1);
    }
    return name;
  }

  private static boolean isSupportedExternalOrigin(
      String qualifiedName,
      Collection<String> supportedQualifiedNames,
      Set<String> sourceDeclaredTypeNames) {
    return qualifiedName != null
        && supportedQualifiedNames.contains(qualifiedName)
        && !sourceDeclaredTypeNames.contains(qualifiedName);
  }

  private static String qualifiedTypeName(String packageName, String simpleName) {
    if (packageName == null || packageName.isBlank()) {
      return simpleName;
    }
    return packageName + "." + simpleName;
  }
}
