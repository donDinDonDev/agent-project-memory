package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class SpringAnnotationOrigins {
  private SpringAnnotationOrigins() {
  }

  static Map<String, String> importsBySimpleName(CompilationUnit compilationUnit) {
    Map<String, String> imports = new LinkedHashMap<>();
    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
      if (importDeclaration.isAsterisk()) {
        continue;
      }
      String importName = importDeclaration.getNameAsString();
      imports.putIfAbsent(simpleName(importName), importName);
    }
    return imports;
  }

  static Optional<String> supportedSimpleName(
      AnnotationExpr annotation,
      Map<String, String> importsBySimpleName,
      Map<String, String> supportedAnnotations) {
    String annotationName = annotation.getNameAsString();
    String simpleName = simpleName(annotationName);
    String supportedQualifiedName = supportedAnnotations.get(simpleName);
    if (supportedQualifiedName == null) {
      return Optional.empty();
    }

    if (annotationName.contains(".")) {
      if (supportedQualifiedName.equals(annotationName)) {
        return Optional.of(simpleName);
      }
      return Optional.empty();
    }

    String importedQualifiedName = importsBySimpleName.get(simpleName);
    if (supportedQualifiedName.equals(importedQualifiedName)) {
      return Optional.of(simpleName);
    }
    return Optional.empty();
  }

  static String simpleAnnotationName(AnnotationExpr annotation) {
    return simpleName(annotation.getNameAsString());
  }

  private static String simpleName(String name) {
    int lastDot = name.lastIndexOf('.');
    if (lastDot >= 0) {
      return name.substring(lastDot + 1);
    }
    return name;
  }
}
