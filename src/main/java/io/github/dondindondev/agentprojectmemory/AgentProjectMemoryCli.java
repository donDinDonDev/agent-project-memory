package io.github.dondindondev.agentprojectmemory;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.springmvc.SpringMvcEndpointOutputGenerator;
import io.github.dondindondev.agentprojectmemory.cache.IncrementalCacheMetadataWriter;
import io.github.dondindondev.agentprojectmemory.cache.IncrementalCacheMetadataValidator;
import io.github.dondindondev.agentprojectmemory.profiles.AgentOutputProfile;
import io.github.dondindondev.agentprojectmemory.scanconfig.InvalidScanConfigException;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfiguration;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfigurationLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public final class AgentProjectMemoryCli {
  public static final String USAGE =
      "Usage: agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>] [--incremental]";
  private static final String GENERAL_HELP = """
      agent-project-memory - local evidence-backed project memory for Java/Spring projects

      Usage:
        agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>] [--incremental]
        agent-project-memory help
        agent-project-memory version

      Commands:
        scan <path> [--config <path>] [--agent-profile <profile>] [--incremental]
                                       Generate .project-memory output for a local repository.
        help                           Show this help.
        version                        Show the CLI version.

      Options:
        --help                         Show this help.
        --version                      Show the CLI version.
      """;
  private static final String SCAN_HELP = """
      Usage: agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>] [--incremental]

      Generate local evidence-backed project memory under <path>/.project-memory/.

      Options:
        --config <path>            Use a repository-relative YAML config file under the scan root.
        --agent-profile <profile>  Generate opt-in profile artifacts for codex, claude, cursor,
                                   generic, or all. May be repeated.
        --incremental              Reuse a validated whole-output cache hit when safe; otherwise
                                   run a full scan and refresh .project-memory/cache/v1/.
        --help                     Show this help.
      """;
  private static final String VERSION_RESOURCE = "/agent-project-memory-version.properties";
  private static final String OUTPUT_DIRECTORY_NAME = ".project-memory";
  private static final int SUCCESS = 0;
  private static final int INTERNAL_ERROR = 1;
  private static final int USAGE_ERROR = 2;
  private static final int SCAN_INPUT_ERROR = 3;
  private static final int INVALID_CONFIG = 4;
  private static final int OUTPUT_ERROR = 5;

  private final PrintWriter out;
  private final PrintWriter err;
  private final ProjectMemoryOutputGenerator outputGenerator;
  private final IncrementalCacheMetadataWriter incrementalCacheMetadataWriter;
  private final IncrementalCacheMetadataValidator incrementalCacheMetadataValidator;
  private final ScanConfigurationLoader scanConfigurationLoader = new ScanConfigurationLoader();

  public AgentProjectMemoryCli(PrintWriter out, PrintWriter err) {
    this(out, err, new SpringMvcEndpointOutputGenerator()::generate);
  }

  AgentProjectMemoryCli(
      PrintWriter out,
      PrintWriter err,
      ProjectMemoryOutputGenerator outputGenerator) {
    this.out = Objects.requireNonNull(out, "out");
    this.err = Objects.requireNonNull(err, "err");
    this.outputGenerator = Objects.requireNonNull(outputGenerator, "outputGenerator");
    this.incrementalCacheMetadataWriter = new IncrementalCacheMetadataWriter();
    this.incrementalCacheMetadataValidator = new IncrementalCacheMetadataValidator();
  }

  public int run(String[] args) {
    try {
      return runInternal(args == null ? new String[0] : args);
    } catch (RuntimeException ex) {
      return internalError();
    } finally {
      out.flush();
      err.flush();
    }
  }

  private int runInternal(String[] args) {
    if (args == null || args.length == 0) {
      return usageError("Missing command.");
    }

    String command = args[0];
    if (command == null || command.isBlank()) {
      return usageError("Missing command.");
    }

    if ("--help".equals(command) || "help".equals(command)) {
      if (args.length != 1) {
        return usageError("Unexpected extra arguments.");
      }
      out.print(GENERAL_HELP);
      return SUCCESS;
    }

    if ("--version".equals(command) || "version".equals(command)) {
      if (args.length != 1) {
        return usageError("Unexpected extra arguments.");
      }
      out.println("agent-project-memory " + version());
      return SUCCESS;
    }

    if (!"scan".equals(command)) {
      return usageError("Unknown command.");
    }

    ScanArgs scanArgs = scanArgs(args);
    if (scanArgs.errorMessage() != null) {
      return scanArgs.errorExitCode() == SCAN_INPUT_ERROR
          ? scanInputError(scanArgs.errorMessage())
          : usageError(scanArgs.errorMessage());
    }
    if (scanArgs.help()) {
      out.print(SCAN_HELP);
      return SUCCESS;
    }
    return scan(
        scanArgs.path(),
        scanArgs.configPath(),
        scanArgs.agentProfiles(),
        scanArgs.incremental());
  }

  private ScanArgs scanArgs(String[] args) {
    if (args.length < 2) {
      return ScanArgs.scanInputError("Missing scan path.");
    }
    if ("--help".equals(args[1])) {
      if (args.length != 2) {
        return ScanArgs.usageError("Unexpected extra arguments.");
      }
      return ScanArgs.helpRequested();
    }

    String scanPath = args[1];
    if (scanPath == null || scanPath.isBlank()) {
      return ScanArgs.scanInputError("Scan path must not be blank.");
    }
    if (scanPath.startsWith("--")) {
      return "--config".equals(scanPath)
              || "--agent-profile".equals(scanPath)
              || "--incremental".equals(scanPath)
          ? ScanArgs.usageError("Missing scan path.")
          : ScanArgs.usageError("Unknown flag.");
    }
    String configPath = null;
    EnumSet<AgentOutputProfile> agentProfiles = EnumSet.noneOf(AgentOutputProfile.class);
    boolean incremental = false;
    int index = 2;
    while (index < args.length) {
      String argument = args[index];
      if ("--incremental".equals(argument)) {
        if (incremental) {
          return ScanArgs.usageError("Duplicate --incremental flag.");
        }
        incremental = true;
        index++;
        continue;
      }
      if ("--config".equals(argument)) {
        if (configPath != null) {
          return ScanArgs.usageError("Duplicate --config flag.");
        }
        if (index + 1 >= args.length) {
          return ScanArgs.usageError("Missing --config value.");
        }
        configPath = args[index + 1];
        index += 2;
        continue;
      }
      if ("--agent-profile".equals(argument)) {
        if (index + 1 >= args.length) {
          return ScanArgs.usageError("Missing --agent-profile value.");
        }
        String selector = args[index + 1];
        if ("all".equals(selector)) {
          agentProfiles.addAll(AgentOutputProfile.canonicalOrder());
        } else {
          AgentOutputProfile profile = AgentOutputProfile.fromSelector(selector).orElse(null);
          if (profile == null) {
            return ScanArgs.usageError("Unsupported --agent-profile value.");
          }
          agentProfiles.add(profile);
        }
        index += 2;
        continue;
      }
      return ScanArgs.usageError("Unexpected extra arguments.");
    }
    return ScanArgs.valid(scanPath, configPath, canonicalProfiles(agentProfiles), incremental);
  }

  private List<AgentOutputProfile> canonicalProfiles(EnumSet<AgentOutputProfile> profiles) {
    List<AgentOutputProfile> selectedProfiles = new ArrayList<>();
    for (AgentOutputProfile profile : AgentOutputProfile.canonicalOrder()) {
      if (profiles.contains(profile)) {
        selectedProfiles.add(profile);
      }
    }
    return List.copyOf(selectedProfiles);
  }

  private int scan(
      String rawPath,
      String explicitConfigPath,
      List<AgentOutputProfile> agentProfiles,
      boolean incremental) {
    if (rawPath == null) {
      return scanInputError("Missing scan path.");
    }
    Path projectPath;
    try {
      projectPath = Path.of(rawPath);
    } catch (InvalidPathException ex) {
      return scanInputError("Invalid scan path syntax.");
    }

    if (Files.notExists(projectPath)) {
      return scanInputError("Scan path does not exist.");
    }

    if (!Files.isDirectory(projectPath)) {
      return scanInputError("Scan path is not a directory.");
    }

    Path normalizedProjectPath = projectPath.toAbsolutePath().normalize();
    Path canonicalProjectPath;
    try {
      canonicalProjectPath = ScanPathContainment.canonicalRoot(normalizedProjectPath);
    } catch (IOException ex) {
      return scanInputError("Could not resolve scan root.");
    }

    ScanConfiguration scanConfiguration;
    try {
      scanConfiguration = scanConfigurationLoader.load(
          normalizedProjectPath,
          canonicalProjectPath,
          explicitConfigPath);
    } catch (InvalidScanConfigException ex) {
      return invalidConfigError(ex.getMessage());
    }

    Path outputDirectory = normalizedProjectPath.resolve(OUTPUT_DIRECTORY_NAME);
    if (Files.isSymbolicLink(outputDirectory)) {
      return scanInputError("Output path must not be a symbolic link.");
    }

    if (Files.exists(outputDirectory, LinkOption.NOFOLLOW_LINKS)
        && !Files.isDirectory(outputDirectory, LinkOption.NOFOLLOW_LINKS)) {
      return scanInputError("Output path exists and is not a directory.");
    }

    try {
      Files.createDirectories(outputDirectory);
    } catch (IOException ex) {
      return outputError("Could not create output directory.");
    }

    if (Files.isSymbolicLink(outputDirectory)) {
      return scanInputError("Output path must not be a symbolic link.");
    }

    Path containedOutputDirectory = ScanPathContainment
        .realPathUnderRoot(canonicalProjectPath, outputDirectory)
        .filter(Files::isDirectory)
        .orElse(null);
    if (containedOutputDirectory == null) {
      return scanInputError("Output directory is not contained under scan root.");
    }

    out.println("Prepared .project-memory.");

    if (incremental) {
      IncrementalCacheMetadataValidator.CacheValidationResult cacheValidation =
          incrementalCacheMetadataValidator.validateHit(
              normalizedProjectPath,
              canonicalProjectPath,
              containedOutputDirectory,
              scanConfiguration,
              agentProfiles,
              version());
      if (cacheValidation.hit()) {
        out.println("Reused incremental cache output set.");
        printDiagnosticsSummary(cacheValidation.diagnosticCount());
        return SUCCESS;
      }
    }

    try {
      SpringMvcEndpointOutputGenerator.Result result = outputGenerator.generate(
          normalizedProjectPath,
          containedOutputDirectory,
          scanConfiguration,
          agentProfiles);
      if (result.generated()) {
        out.println(
            "Generated project-map.json with "
                + result.endpointCount()
                + " endpoint facts and "
                + result.componentCount()
                + " component facts and "
                + result.entityCount()
                + " entity facts and "
                + result.testCount()
                + " test facts and "
                + result.documentCount()
                + " document facts.");
        out.println("Generated endpoints.md with " + result.endpointCount() + " endpoint facts.");
        out.println(
            "Generated evidence-index.jsonl with "
                + result.evidenceCount()
                + " evidence records.");
        out.println("Generated project-graph.json.");
        out.println("Generated agent-guide.md.");
        if (result.profileCount() > 0) {
          out.println("Generated agent profile artifacts: " + result.profileCount() + ".");
        }
        if (incremental) {
          IncrementalCacheMetadataWriter.CacheWriteResult cacheResult =
              incrementalCacheMetadataWriter.write(
                  normalizedProjectPath,
                  canonicalProjectPath,
                  containedOutputDirectory,
                  scanConfiguration,
                  agentProfiles,
                  version());
          if (cacheResult.written()) {
            out.println("Updated incremental cache metadata.");
          } else {
            out.println("Skipped incremental cache metadata refresh.");
          }
        }
      } else {
        out.println("No project memory output generated.");
      }
      printDiagnosticsSummary(result.diagnosticCount());
    } catch (IOException ex) {
      String message = boundedExceptionMessage(ex, normalizedProjectPath);
      if (isOutputPathValidationError(message)) {
        return scanInputError(message);
      }
      return outputError("Could not generate project memory output: " + message);
    }

    return SUCCESS;
  }

  private void printDiagnosticsSummary(int diagnosticCount) {
    if (diagnosticCount == 0) {
      out.println("Diagnostics: none.");
    } else {
      out.println("Diagnostics: " + diagnosticCount + " item(s).");
    }
  }

  private int usageError(String message) {
    err.println("Usage error: " + message);
    err.println(USAGE);
    return USAGE_ERROR;
  }

  private int scanInputError(String message) {
    err.println("Scan input error: " + message);
    return SCAN_INPUT_ERROR;
  }

  private int invalidConfigError(String message) {
    err.println(message);
    return INVALID_CONFIG;
  }

  private int outputError(String message) {
    err.println("Output error: " + message);
    return OUTPUT_ERROR;
  }

  private int internalError() {
    err.println("Unexpected internal error.");
    return INTERNAL_ERROR;
  }

  private boolean isOutputPathValidationError(String message) {
    return message.startsWith("Output file must not be a symbolic link:")
        || message.startsWith("Output file target is not a regular file under scan root:")
        || message.startsWith("Output file must not have multiple hard links:")
        || message.startsWith("Output directory must not be a symbolic link:")
        || message.startsWith("Output directory path exists and is not a directory:")
        || message.startsWith("Output directory is not contained under scan root:");
  }

  private String boundedExceptionMessage(IOException exception, Path scanRoot) {
    String message = exception.getMessage();
    if (message == null || message.isBlank()) {
      return "I/O error.";
    }
    return stripLocalRoot(message, scanRoot);
  }

  private String stripLocalRoot(String message, Path scanRoot) {
    String root = scanRoot.toAbsolutePath().normalize().toString();
    String slashRoot = root.replace('\\', '/');
    String result = message;
    result = stripRootVariant(result, root);
    if (!slashRoot.equals(root)) {
      result = stripRootVariant(result, slashRoot);
    }
    return result;
  }

  private String stripRootVariant(String message, String root) {
    String withSlash = root.endsWith("/") ? root : root + "/";
    return message.replace(withSlash, "").replace(root, ".");
  }

  static String version() {
    try (InputStream input = AgentProjectMemoryCli.class.getResourceAsStream(VERSION_RESOURCE)) {
      if (input != null) {
        Properties properties = new Properties();
        properties.load(input);
        String version = properties.getProperty("version");
        if (version != null && !version.isBlank() && !version.contains("${")) {
          return version;
        }
      }
    } catch (IOException ignored) {
      // Fall through to package metadata.
    }

    Package cliPackage = AgentProjectMemoryCli.class.getPackage();
    String implementationVersion = cliPackage == null ? null : cliPackage.getImplementationVersion();
    if (implementationVersion != null && !implementationVersion.isBlank()) {
      return implementationVersion;
    }
    return "unknown";
  }

  @FunctionalInterface
  interface ProjectMemoryOutputGenerator {
    SpringMvcEndpointOutputGenerator.Result generate(
        Path repositoryRoot,
        Path outputDirectory,
        ScanConfiguration scanConfiguration,
        List<AgentOutputProfile> agentProfiles) throws IOException;
  }

  private record ScanArgs(
      String path,
      String configPath,
      List<AgentOutputProfile> agentProfiles,
      boolean incremental,
      boolean help,
      String errorMessage,
      int errorExitCode) {
    static ScanArgs valid(
        String path,
        String configPath,
        List<AgentOutputProfile> agentProfiles,
        boolean incremental) {
      return new ScanArgs(path, configPath, agentProfiles, incremental, false, null, SUCCESS);
    }

    static ScanArgs helpRequested() {
      return new ScanArgs(null, null, List.of(), false, true, null, SUCCESS);
    }

    static ScanArgs usageError(String message) {
      return new ScanArgs(null, null, List.of(), false, false, message, USAGE_ERROR);
    }

    static ScanArgs scanInputError(String message) {
      return new ScanArgs(null, null, List.of(), false, false, message, SCAN_INPUT_ERROR);
    }
  }
}
