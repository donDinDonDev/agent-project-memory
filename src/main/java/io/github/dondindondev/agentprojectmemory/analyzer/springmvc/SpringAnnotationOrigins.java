package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SpringAnnotationOrigins {
  private SpringAnnotationOrigins() {
  }

  static Map<String, String> importsBySimpleName(CompilationUnit compilationUnit) {
    return JavaSourceOrigins.singleTypeImportsBySimpleName(compilationUnit);
  }

  static Optional<String> supportedSimpleName(
      AnnotationExpr annotation,
      Map<String, String> importsBySimpleName,
      Map<String, ? extends Collection<String>> supportedAnnotations,
      Set<String> sourceDeclaredTypeNames) {
    return JavaSourceOrigins.supportedAnnotationSimpleName(
        annotation,
        importsBySimpleName,
        supportedAnnotations,
        sourceDeclaredTypeNames);
  }

  static String simpleAnnotationName(AnnotationExpr annotation) {
    return JavaSourceOrigins.simpleAnnotationName(annotation);
  }
}
