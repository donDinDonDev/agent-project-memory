package io.github.dondindondev.agentprojectmemory.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Problem;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class JavaSourceParser {
  private JavaSourceParser() {
  }

  public static CompilationUnit parse(Path javaFile) throws IOException {
    Objects.requireNonNull(javaFile, "javaFile");

    ParseResult<CompilationUnit> result = new JavaParser(parserConfiguration()).parse(javaFile);
    Optional<CompilationUnit> compilationUnit = result.getResult();
    if (!result.isSuccessful() || compilationUnit.isEmpty()) {
      throw parseFailure(javaFile, result);
    }
    return compilationUnit.orElseThrow();
  }

  private static IOException parseFailure(
      Path javaFile,
      ParseResult<CompilationUnit> result) {
    int problemCount = result.getProblems().size();
    return new IOException(
        "Could not parse Java source: "
            + javaFile.toAbsolutePath().normalize()
            + " ("
            + problemCount
            + " parse "
            + problemLabel(problemCount)
            + firstProblemLocation(result)
            + ").");
  }

  private static String problemLabel(int problemCount) {
    return problemCount == 1 ? "problem" : "problems";
  }

  private static String firstProblemLocation(ParseResult<CompilationUnit> result) {
    return result.getProblems().stream()
        .findFirst()
        .flatMap(Problem::getLocation)
        .flatMap(TokenRange::toRange)
        .map(JavaSourceParser::locationLabel)
        .orElse("; first problem location unavailable");
  }

  private static String locationLabel(Range range) {
    return "; first problem at line " + range.begin.line + ", column " + range.begin.column;
  }

  private static ParserConfiguration parserConfiguration() {
    return new ParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
  }
}
