package io.github.dondindondev.agentprojectmemory;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.springmvc.SpringMvcEndpointOutputGenerator;
import io.github.dondindondev.agentprojectmemory.ai.AiPresentationOptions;
import io.github.dondindondev.agentprojectmemory.cache.IncrementalCacheMetadataWriter;
import io.github.dondindondev.agentprojectmemory.cache.IncrementalCacheMetadataValidator;
import io.github.dondindondev.agentprojectmemory.profiles.AgentOutputProfile;
import io.github.dondindondev.agentprojectmemory.query.ProjectMemoryArtifactReader;
import io.github.dondindondev.agentprojectmemory.query.ProjectMemoryAgentContextRenderer;
import io.github.dondindondev.agentprojectmemory.query.ProjectMemoryArtifacts;
import io.github.dondindondev.agentprojectmemory.query.ProjectMemoryListRenderer;
import io.github.dondindondev.agentprojectmemory.query.ProjectMemoryLookupRenderer;
import io.github.dondindondev.agentprojectmemory.query.ProjectMemoryRelationRenderer;
import io.github.dondindondev.agentprojectmemory.query.QueryArtifactException;
import io.github.dondindondev.agentprojectmemory.scanconfig.InvalidScanConfigException;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfiguration;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfigurationLoader;
import io.github.dondindondev.agentprojectmemory.workspace.InvalidWorkspaceConfigException;
import io.github.dondindondev.agentprojectmemory.workspace.WorkspaceConfiguration;
import io.github.dondindondev.agentprojectmemory.workspace.WorkspaceConfigurationLoader;
import io.github.dondindondev.agentprojectmemory.workspace.WorkspaceMapGenerator;
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
      "Usage: agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>] "
          + "[--ai-presentation mock_no_network] [--incremental]\n"
          + "       agent-project-memory workspace scan <config>";
  private static final String GENERAL_HELP = """
      agent-project-memory - local evidence-backed project memory for Java/Spring projects

      Usage:
        agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>] [--ai-presentation mock_no_network] [--incremental]
        agent-project-memory workspace scan <config>
        agent-project-memory query <path> <query-command>
        agent-project-memory help
        agent-project-memory version

      Commands:
        scan <path> [--config <path>] [--agent-profile <profile>] [--ai-presentation mock_no_network] [--incremental]
                                       Generate .project-memory output for a local repository.
        workspace scan <config>        Generate a workspace map from existing member artifacts.
        query <path> <query-command>   Validate and read existing .project-memory artifacts.
        help                           Show this help.
        version                        Show the CLI version.

      Options:
        --help                         Show this help.
        --version                      Show the CLI version.
      """;
  private static final String SCAN_HELP = """
      Usage: agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>] [--ai-presentation mock_no_network] [--incremental]

      Generate local evidence-backed project memory under <path>/.project-memory/.

      Options:
        --config <path>            Use a repository-relative YAML config file under the scan root.
        --agent-profile <profile>  Generate opt-in profile artifacts for codex, claude, cursor,
                                   generic, or all. May be repeated.
        --ai-presentation mock_no_network
                                   Generate opt-in non-authoritative AI presentation artifacts
                                   with the mock/no-network provider.
        --incremental              Reuse a validated whole-output cache hit when safe; otherwise
                                   run a full scan and refresh .project-memory/cache/v1/.
        --help                     Show this help.
      """;
  private static final String WORKSPACE_USAGE =
      "Usage: agent-project-memory workspace scan <config>";
  private static final String WORKSPACE_HELP = """
      Usage: agent-project-memory workspace scan <config>

      Validate an explicit local workspace YAML config and write a workspace-root
      .project-memory/workspace-map.json from existing member artifacts. The command
      does not run or refresh child repository scans.

      Options:
        --help                     Show this help.
      """;
  private static final String QUERY_USAGE = "Usage: agent-project-memory query <path> <query-command>";
  private static final String QUERY_HELP = """
      Usage: agent-project-memory query <path> <query-command>

      Read existing .project-memory artifacts without scanning or writing.

      Query commands:
        list modules               List generated modules.
        list endpoints             List source-visible Spring MVC endpoints.
        list api-operations        List spec-backed declared API operations.
        list entities              List JPA entities, embeddables, and repository/entity relation rows.
        list tests                 List emitted test facts and tested-subject rows.
        explain evidence <id>      Explain one evidence-index.jsonl record by exact ID.
        find fact <term>           Find generated fact IDs and exact keys.
        find symbol <term>         Find structured symbol fields.
        relations <id> [--direction incoming|outgoing|both]
                                   Show one-hop graph relations.
        agent-context              Render bounded read-only agent context over existing artifacts.

      Options:
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
  private static final int QUERY_NO_RESULT = 6;

  private final PrintWriter out;
  private final PrintWriter err;
  private final ProjectMemoryOutputGenerator outputGenerator;
  private final IncrementalCacheMetadataWriter incrementalCacheMetadataWriter;
  private final IncrementalCacheMetadataValidator incrementalCacheMetadataValidator;
  private final ProjectMemoryArtifactReader queryArtifactReader = new ProjectMemoryArtifactReader();
  private final ProjectMemoryListRenderer queryListRenderer = new ProjectMemoryListRenderer();
  private final ProjectMemoryLookupRenderer queryLookupRenderer = new ProjectMemoryLookupRenderer();
  private final ProjectMemoryRelationRenderer queryRelationRenderer =
      new ProjectMemoryRelationRenderer();
  private final ProjectMemoryAgentContextRenderer queryAgentContextRenderer =
      new ProjectMemoryAgentContextRenderer();
  private final ScanConfigurationLoader scanConfigurationLoader = new ScanConfigurationLoader();
  private final WorkspaceConfigurationLoader workspaceConfigurationLoader =
      new WorkspaceConfigurationLoader();
  private final WorkspaceMapGenerator workspaceMapGenerator = new WorkspaceMapGenerator();

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

    if ("query".equals(command)) {
      QueryArgs queryArgs = queryArgs(args);
      if (queryArgs.errorMessage() != null) {
        return queryArgs.errorExitCode() == SCAN_INPUT_ERROR
            ? queryInputError(queryArgs.errorMessage())
            : queryUsageError(queryArgs.errorMessage());
      }
      if (queryArgs.help()) {
        out.print(QUERY_HELP);
        return SUCCESS;
      }
      return query(queryArgs);
    }

    if ("workspace".equals(command)) {
      WorkspaceArgs workspaceArgs = workspaceArgs(args);
      if (workspaceArgs.errorMessage() != null) {
        return workspaceArgs.errorExitCode() == INVALID_CONFIG
            ? invalidConfigError(workspaceArgs.errorMessage())
            : workspaceUsageError(workspaceArgs.errorMessage());
      }
      if (workspaceArgs.help()) {
        out.print(WORKSPACE_HELP);
        return SUCCESS;
      }
      return workspaceScan(workspaceArgs.configPath());
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
        scanArgs.aiPresentationOptions(),
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
              || "--ai-presentation".equals(scanPath)
              || "--incremental".equals(scanPath)
          ? ScanArgs.usageError("Missing scan path.")
          : ScanArgs.usageError("Unknown flag.");
    }
    String configPath = null;
    EnumSet<AgentOutputProfile> agentProfiles = EnumSet.noneOf(AgentOutputProfile.class);
    AiPresentationOptions aiPresentationOptions = AiPresentationOptions.disabled();
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
      if ("--ai-presentation".equals(argument)) {
        if (aiPresentationOptions.enabled()) {
          return ScanArgs.usageError("Duplicate --ai-presentation flag.");
        }
        if (index + 1 >= args.length) {
          return ScanArgs.usageError("Missing --ai-presentation value.");
        }
        aiPresentationOptions = AiPresentationOptions.fromCliValue(args[index + 1]).orElse(null);
        if (aiPresentationOptions == null) {
          return ScanArgs.usageError("Unsupported --ai-presentation value.");
        }
        index += 2;
        continue;
      }
      return ScanArgs.usageError("Unexpected extra arguments.");
    }
    return ScanArgs.valid(
        scanPath,
        configPath,
        canonicalProfiles(agentProfiles),
        aiPresentationOptions,
        incremental);
  }

  private WorkspaceArgs workspaceArgs(String[] args) {
    if (args.length < 2) {
      return WorkspaceArgs.usageError("Missing workspace subcommand.");
    }
    if ("--help".equals(args[1])) {
      if (args.length != 2) {
        return WorkspaceArgs.usageError("Unexpected extra arguments.");
      }
      return WorkspaceArgs.helpRequested();
    }
    if (!"scan".equals(args[1])) {
      return WorkspaceArgs.usageError("Unsupported workspace subcommand.");
    }
    if (args.length < 3) {
      return WorkspaceArgs.usageError("Missing workspace config path.");
    }
    if ("--help".equals(args[2])) {
      if (args.length != 3) {
        return WorkspaceArgs.usageError("Unexpected extra arguments.");
      }
      return WorkspaceArgs.helpRequested();
    }
    if (args[2] == null || args[2].isBlank()) {
      return WorkspaceArgs.invalidConfig("Invalid workspace config: config path must not be blank.");
    }
    if (args[2].startsWith("--")) {
      return WorkspaceArgs.usageError("Missing workspace config path.");
    }
    if (args.length != 3) {
      return WorkspaceArgs.usageError("Unexpected extra arguments.");
    }
    return WorkspaceArgs.valid(args[2]);
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

  private QueryArgs queryArgs(String[] args) {
    if (args.length < 2) {
      return QueryArgs.queryInputError("Missing query path.");
    }
    if ("--help".equals(args[1])) {
      if (args.length != 2) {
        return QueryArgs.usageError("Unexpected extra arguments.");
      }
      return QueryArgs.helpRequested();
    }

    String queryPath = args[1];
    if (queryPath == null || queryPath.isBlank()) {
      return QueryArgs.queryInputError("Query path must not be blank.");
    }
    if (queryPath.startsWith("--")) {
      return QueryArgs.usageError("Unknown flag.");
    }
    if (args.length < 3) {
      return QueryArgs.usageError("Missing query subcommand.");
    }
    if ("--help".equals(args[2])) {
      if (args.length != 3) {
        return QueryArgs.usageError("Unexpected extra arguments.");
      }
      return QueryArgs.helpRequested();
    }

    String subcommand = args[2];
    if ("list".equals(subcommand)) {
      if (args.length != 4) {
        return QueryArgs.usageError("Malformed list query command.");
      }
      if (!isSupportedListSubject(args[3])) {
        return QueryArgs.usageError("Unsupported query list subject.");
      }
      return QueryArgs.valid(
          queryPath,
          "list",
          args[3],
          null,
          ProjectMemoryArtifactReader.GraphRequirement.NONE,
          ProjectMemoryRelationRenderer.Direction.BOTH);
    }
    if ("explain".equals(subcommand)) {
      if (args.length != 5 || !"evidence".equals(args[3])) {
        return QueryArgs.usageError("Malformed explain query command.");
      }
      if (args[4] == null || args[4].isBlank() || args[4].startsWith("--")) {
        return QueryArgs.usageError("Missing evidence id.");
      }
      return QueryArgs.valid(
          queryPath,
          "explain",
          "evidence",
          args[4],
          ProjectMemoryArtifactReader.GraphRequirement.NONE,
          ProjectMemoryRelationRenderer.Direction.BOTH);
    }
    if ("find".equals(subcommand)) {
      if (args.length != 5 || (!"fact".equals(args[3]) && !"symbol".equals(args[3]))) {
        return QueryArgs.usageError("Malformed find query command.");
      }
      if (args[4] == null || args[4].isBlank() || args[4].startsWith("--")) {
        return QueryArgs.usageError("Missing find term.");
      }
      return QueryArgs.valid(
          queryPath,
          "find",
          args[3],
          args[4],
          graphRequirementForFind(args[3], args[4]),
          ProjectMemoryRelationRenderer.Direction.BOTH);
    }
    if ("relations".equals(subcommand)) {
      if (args.length < 4) {
        return QueryArgs.usageError("Malformed relations query command.");
      }
      String relationId = args[3];
      if (relationId == null || relationId.isBlank() || relationId.startsWith("--")) {
        return QueryArgs.usageError("Missing relation id.");
      }
      ProjectMemoryRelationRenderer.Direction direction =
          ProjectMemoryRelationRenderer.Direction.BOTH;
      boolean directionSeen = false;
      int index = 4;
      while (index < args.length) {
        String argument = args[index];
        if (!"--direction".equals(argument)) {
          return argument != null && argument.startsWith("--")
              ? QueryArgs.usageError("Unknown flag.")
              : QueryArgs.usageError("Unexpected extra arguments.");
        }
        if (directionSeen) {
          return QueryArgs.usageError("Duplicate --direction flag.");
        }
        if (index + 1 >= args.length) {
          return QueryArgs.usageError("Missing --direction value.");
        }
        direction = ProjectMemoryRelationRenderer.Direction.fromCliValue(args[index + 1])
            .orElse(null);
        if (direction == null) {
          return QueryArgs.usageError("Unsupported --direction value.");
        }
        directionSeen = true;
        index += 2;
      }
      return QueryArgs.valid(
          queryPath,
          "relations",
          "relations",
          relationId,
          ProjectMemoryArtifactReader.GraphRequirement.REQUIRED,
          direction);
    }
    if ("agent-context".equals(subcommand)) {
      if (args.length != 3) {
        return QueryArgs.usageError("Malformed agent-context query command.");
      }
      return QueryArgs.valid(
          queryPath,
          "agent-context",
          "agent-context",
          null,
          ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL,
          ProjectMemoryRelationRenderer.Direction.BOTH);
    }
    return QueryArgs.usageError("Unsupported query subcommand.");
  }

  private boolean isSupportedListSubject(String subject) {
    return "modules".equals(subject)
        || "endpoints".equals(subject)
        || "api-operations".equals(subject)
        || "entities".equals(subject)
        || "tests".equals(subject);
  }

  private ProjectMemoryArtifactReader.GraphRequirement graphRequirementForFind(
      String subject,
      String term) {
    if (!"fact".equals(subject) || term == null) {
      return ProjectMemoryArtifactReader.GraphRequirement.NONE;
    }
    return isGraphFactTerm(term)
        ? ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL
        : ProjectMemoryArtifactReader.GraphRequirement.NONE;
  }

  private boolean isGraphFactTerm(String term) {
    return term.startsWith("node:")
        || term.startsWith("edge:")
        || term.startsWith("relation-status:")
        || term.startsWith("graph-warning:");
  }

  private int query(QueryArgs queryArgs) {
    Path queryPath;
    try {
      queryPath = Path.of(queryArgs.path());
    } catch (InvalidPathException ex) {
      return queryInputError("Invalid query path syntax.");
    }

    ProjectMemoryArtifacts artifacts;
    try {
      artifacts = queryArtifactReader.load(queryPath, queryArgs.graphRequirement());
    } catch (QueryArtifactException ex) {
      return queryInputError(ex.getMessage());
    }

    if ("list".equals(queryArgs.command())) {
      out.print(queryListRenderer.render(artifacts, queryArgs.subject()));
      return SUCCESS;
    }
    if ("explain".equals(queryArgs.command())) {
      ProjectMemoryLookupRenderer.LookupResult result =
          queryLookupRenderer.renderEvidence(artifacts, queryArgs.lookupTerm());
      return printLookupResult(result);
    }
    if ("find".equals(queryArgs.command())) {
      ProjectMemoryLookupRenderer.FindKind kind = "fact".equals(queryArgs.subject())
          ? ProjectMemoryLookupRenderer.FindKind.FACT
          : ProjectMemoryLookupRenderer.FindKind.SYMBOL;
      ProjectMemoryLookupRenderer.LookupResult result =
          queryLookupRenderer.renderFind(artifacts, kind, queryArgs.lookupTerm());
      return printLookupResult(result);
    }
    if ("relations".equals(queryArgs.command())) {
      ProjectMemoryLookupRenderer.LookupResult result = queryRelationRenderer.render(
          artifacts,
          queryArgs.lookupTerm(),
          queryArgs.relationDirection());
      return printLookupResult(result);
    }
    if ("agent-context".equals(queryArgs.command())) {
      out.print(queryAgentContextRenderer.render(artifacts));
      return SUCCESS;
    }

    out.println("Query artifact validation succeeded.");
    out.println("Loaded project-map.json schema_version " + artifacts.projectMapSchemaVersion() + ".");
    out.println(
        "Loaded evidence-index.jsonl with "
            + artifacts.evidenceRecords().size()
            + " evidence record(s).");
    if (artifacts.hasProjectGraph()) {
      out.println(
          "Loaded project-graph.json graph_schema_version "
              + artifacts.projectGraphSchemaVersion()
              + ".");
    } else {
      out.println("No project-graph.json loaded.");
    }
    String queryLabel = queryArgs.command().equals(queryArgs.subject())
        ? queryArgs.command()
        : queryArgs.command() + " " + queryArgs.subject();
    out.println(
        "Result rendering for query "
            + queryLabel
            + " is not implemented in this foundation.");
    return SUCCESS;
  }

  private int workspaceScan(String rawConfigPath) {
    WorkspaceConfiguration configuration;
    try {
      configuration = workspaceConfigurationLoader.load(rawConfigPath);
    } catch (InvalidWorkspaceConfigException ex) {
      return invalidConfigError(ex.getMessage());
    }

    WorkspaceMapGenerator.Result result;
    try {
      result = workspaceMapGenerator.generate(configuration);
    } catch (IOException ex) {
      String message = boundedExceptionMessage(
          ex,
          configuration.workspaceRoot(),
          configuration.canonicalWorkspaceRoot());
      if (isOutputPathValidationError(message)) {
        return scanInputError(message);
      }
      return outputError("Could not generate workspace map: " + message);
    }

    out.println("Workspace config validated.");
    out.println("Workspace members: " + result.memberCount() + ".");
    out.println("Generated workspace-map.json.");
    printDiagnosticsSummary(result.diagnosticCount());
    return SUCCESS;
  }

  private int scan(
      String rawPath,
      String explicitConfigPath,
      List<AgentOutputProfile> agentProfiles,
      AiPresentationOptions aiPresentationOptions,
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
    boolean adapterSelected = scanConfiguration.adapterConfiguration().enabled();
    boolean aiPresentationSelected = aiPresentationOptions.enabled();

    if (incremental && !adapterSelected && !aiPresentationSelected) {
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
          agentProfiles,
          aiPresentationOptions);
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
        if (result.sourceRegistryGenerated()) {
          out.println(
              "Generated source-registry.json with "
                  + result.sourceDocumentCount()
                  + " source document(s) and "
                  + result.adapterDiagnosticCount()
                  + " adapter diagnostic(s).");
        }
        out.println("Generated agent-guide.md.");
        if (result.profileCount() > 0) {
          out.println("Generated agent profile artifacts: " + result.profileCount() + ".");
        }
        if (result.aiPresentationGenerated()) {
          out.println("Generated AI presentation artifacts with mock_no_network provider.");
        }
        if (incremental) {
          if (adapterSelected || aiPresentationSelected) {
            out.println("Skipped incremental cache metadata refresh.");
          } else {
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
        }
      } else {
        out.println("No project memory output generated.");
      }
      printDiagnosticsSummary(result.diagnosticCount());
    } catch (IOException ex) {
      String message = boundedExceptionMessage(ex, normalizedProjectPath, canonicalProjectPath);
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

  private int queryInputError(String message) {
    err.println("Query input error: " + message);
    return SCAN_INPUT_ERROR;
  }

  private int printLookupResult(ProjectMemoryLookupRenderer.LookupResult result) {
    if (!result.found()) {
      return queryNoResult(result.noResultMessage());
    }
    out.print(result.output());
    return SUCCESS;
  }

  private int queryNoResult(String message) {
    err.println("Query no result: " + message);
    return QUERY_NO_RESULT;
  }

  private int queryUsageError(String message) {
    err.println("Usage error: " + message);
    err.println(QUERY_USAGE);
    return USAGE_ERROR;
  }

  private int workspaceUsageError(String message) {
    err.println("Usage error: " + message);
    err.println(WORKSPACE_USAGE);
    return USAGE_ERROR;
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

  private String boundedExceptionMessage(IOException exception, Path... scanRoots) {
    String message = exception.getMessage();
    if (message == null || message.isBlank()) {
      return "I/O error.";
    }
    return OutputRedactor.redact(stripLocalRoots(message, scanRoots));
  }

  private String stripLocalRoots(String message, Path... scanRoots) {
    List<String> roots = new ArrayList<>();
    for (Path scanRoot : scanRoots) {
      if (scanRoot == null) {
        continue;
      }
      String root = scanRoot.toAbsolutePath().normalize().toString();
      addRootVariant(roots, root);
      addRootVariant(roots, root.replace('\\', '/'));
    }
    roots.sort((left, right) -> Integer.compare(right.length(), left.length()));
    String result = message;
    for (String root : roots) {
      result = stripRootVariant(result, root);
    }
    return result;
  }

  private void addRootVariant(List<String> roots, String root) {
    if (!root.isBlank() && !roots.contains(root)) {
      roots.add(root);
    }
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
        List<AgentOutputProfile> agentProfiles,
        AiPresentationOptions aiPresentationOptions) throws IOException;
  }

  private record ScanArgs(
      String path,
      String configPath,
      List<AgentOutputProfile> agentProfiles,
      AiPresentationOptions aiPresentationOptions,
      boolean incremental,
      boolean help,
      String errorMessage,
      int errorExitCode) {
    static ScanArgs valid(
        String path,
        String configPath,
        List<AgentOutputProfile> agentProfiles,
        AiPresentationOptions aiPresentationOptions,
        boolean incremental) {
      return new ScanArgs(
          path,
          configPath,
          agentProfiles,
          aiPresentationOptions,
          incremental,
          false,
          null,
          SUCCESS);
    }

    static ScanArgs helpRequested() {
      return new ScanArgs(
          null,
          null,
          List.of(),
          AiPresentationOptions.disabled(),
          false,
          true,
          null,
          SUCCESS);
    }

    static ScanArgs usageError(String message) {
      return new ScanArgs(
          null,
          null,
          List.of(),
          AiPresentationOptions.disabled(),
          false,
          false,
          message,
          USAGE_ERROR);
    }

    static ScanArgs scanInputError(String message) {
      return new ScanArgs(
          null,
          null,
          List.of(),
          AiPresentationOptions.disabled(),
          false,
          false,
          message,
          SCAN_INPUT_ERROR);
    }
  }

  private record QueryArgs(
      String path,
      String command,
      String subject,
      String lookupTerm,
      ProjectMemoryArtifactReader.GraphRequirement graphRequirement,
      ProjectMemoryRelationRenderer.Direction relationDirection,
      boolean help,
      String errorMessage,
      int errorExitCode) {
    static QueryArgs valid(
        String path,
        String command,
        String subject,
        String lookupTerm,
        ProjectMemoryArtifactReader.GraphRequirement graphRequirement,
        ProjectMemoryRelationRenderer.Direction relationDirection) {
      return new QueryArgs(
          path,
          command,
          subject,
          lookupTerm,
          graphRequirement,
          relationDirection,
          false,
          null,
          SUCCESS);
    }

    static QueryArgs helpRequested() {
      return new QueryArgs(
          null,
          null,
          null,
          null,
          ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL,
          ProjectMemoryRelationRenderer.Direction.BOTH,
          true,
          null,
          SUCCESS);
    }

    static QueryArgs usageError(String message) {
      return new QueryArgs(
          null,
          null,
          null,
          null,
          ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL,
          ProjectMemoryRelationRenderer.Direction.BOTH,
          false,
          message,
          USAGE_ERROR);
    }

    static QueryArgs queryInputError(String message) {
      return new QueryArgs(
          null,
          null,
          null,
          null,
          ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL,
          ProjectMemoryRelationRenderer.Direction.BOTH,
          false,
          message,
          SCAN_INPUT_ERROR);
    }
  }

  private record WorkspaceArgs(
      String configPath,
      boolean help,
      String errorMessage,
      int errorExitCode) {
    static WorkspaceArgs valid(String configPath) {
      return new WorkspaceArgs(configPath, false, null, SUCCESS);
    }

    static WorkspaceArgs helpRequested() {
      return new WorkspaceArgs(null, true, null, SUCCESS);
    }

    static WorkspaceArgs usageError(String message) {
      return new WorkspaceArgs(null, false, message, USAGE_ERROR);
    }

    static WorkspaceArgs invalidConfig(String message) {
      return new WorkspaceArgs(null, false, message, INVALID_CONFIG);
    }
  }
}
