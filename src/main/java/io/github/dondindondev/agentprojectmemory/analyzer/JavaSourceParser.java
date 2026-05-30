package io.github.dondindondev.agentprojectmemory.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class JavaSourceParser {
  private JavaSourceParser() {
  }

  public static CompilationUnit parse(Path javaFile) throws IOException {
    Objects.requireNonNull(javaFile, "javaFile");

    ParseResult<CompilationUnit> result = new JavaParser(parserConfiguration()).parse(javaFile);
    if (!result.isSuccessful()) {
      throw new ParseProblemException(result.getProblems());
    }
    return result.getResult().orElseThrow(() -> new ParseProblemException(result.getProblems()));
  }

  private static ParserConfiguration parserConfiguration() {
    return new ParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
  }
}
