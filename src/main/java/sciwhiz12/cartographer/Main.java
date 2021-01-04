package sciwhiz12.cartographer;

import com.google.common.base.Stopwatch;
import sciwhiz12.cartographer.mcp.MCPDatabase;
import sciwhiz12.cartographer.srg.SRGDatabase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Path.of;
import static java.nio.file.StandardOpenOption.CREATE;
import static sciwhiz12.cartographer.util.Logging.logf;

public class Main {

    public static final Path TSRG = of("config/joined.tsrg");
    public static final Path CONSTRUCTORS = of("config/constructors.txt");
    public static final Path STATIC_METHODS = of("config/static_methods.txt");

    public static final Path FIELDS = of("mcp/fields.csv");
    public static final Path METHODS = of("mcp/methods.csv");
    public static final Path PARAMS = of("mcp/params.csv");

    public static final Path SRG_DATABASE_OUTPUT = of("srg_database.txt");

    public static void main(String[] args) throws Exception {
        boolean deserializeSRGs = Files.exists(SRG_DATABASE_OUTPUT);
        SRGDatabase srgDatabase = null;
        MCPDatabase mcpDatabase;
        Stopwatch stopwatch = null;
        if (deserializeSRGs) {
            System.out.println("SRG database exists on disk, deserializing...");

            stopwatch = Stopwatch.createStarted();
            List<String> dbFile = Files.readAllLines(SRG_DATABASE_OUTPUT);
            srgDatabase = SRGDatabase.deserialize(dbFile);
            stopwatch.stop();

            logf(" === SRG Deserialize === %n");
            srgDatabase.printStatistics(System.out);
            logf(" === === === === === === %n");

            System.out.printf("Time elapsed for deserialization: %s%n", stopwatch.elapsed());
        }
        if (srgDatabase == null) {
            System.out.println("Importing SRG database from MCPConfig files...");

            stopwatch = Stopwatch.createStarted();
            List<String> joined_tsrg = Files.readAllLines(TSRG);
            List<String> static_methods_txt = Files.readAllLines(STATIC_METHODS);
            List<String> constructors_txt = Files.readAllLines(CONSTRUCTORS);
            srgDatabase = SRGDatabase.parse(joined_tsrg, static_methods_txt, constructors_txt);
            stopwatch.stop();

            logf(" === SRG Import === %n");
            srgDatabase.printStatistics(System.out);
            logf(" === === == === === %n");

            System.out.printf("Time elapsed for import: %s%n", stopwatch.elapsed());
            System.out.println();
            System.out.println("Exporting imported SRG database to disk...");

            stopwatch = Stopwatch.createStarted();
            Files.write(SRG_DATABASE_OUTPUT, srgDatabase.serialize(), CREATE);
            stopwatch.stop();

            System.out.printf("Time elapsed for export: %s%n", stopwatch.elapsed());
        }

        {
            System.out.println();
            System.out.println("Importing MCP database from mappings files...");

            List<String> fields_csv = Files.readAllLines(FIELDS);
            List<String> methods_csv = Files.readAllLines(METHODS);
            List<String> params_csv = Files.readAllLines(PARAMS);
            mcpDatabase = MCPDatabase.parse(fields_csv, methods_csv, params_csv);

            System.out.printf("Time elapsed for MCP database import: %s%n", stopwatch.elapsed());
        }

        System.out.println();
        System.out.println("Ready!");
        QueryConsole console = new QueryConsole(srgDatabase, mcpDatabase, System.in);
        console.run();
    }

    static boolean compareDatabases(SRGDatabase primary, SRGDatabase secondary, boolean printComparisons) {
        boolean classes = primary.classes().equals(secondary.classes());
        boolean fields = primary.fields().equals(secondary.fields());
        boolean enumValues = primary.enumValues().equals(secondary.enumValues());
        boolean namedMethods = primary.namedMethods().equals(secondary.namedMethods());
        boolean numberedMethods = primary.numberedMethods().equals(secondary.numberedMethods());
        boolean constructors = primary.constructors().equals(secondary.constructors());
        boolean namedMethodParameters = primary.namedMethodParameters().equals(secondary.namedMethodParameters());
        boolean numberedMethodParameters = primary.numberedMethodParameters().equals(secondary.numberedMethodParameters());
        boolean constructorParameters = primary.constructorParameters().equals(secondary.constructorParameters());
        if (printComparisons) {
            System.out.printf("    classes: %s%n", classes);
            System.out.printf("    fields: %s, enum values: %s%n", fields, enumValues);
            System.out.printf("    named methods: %s, parameters: %s%n", namedMethods, namedMethodParameters);
            System.out.printf("    numbered methods: %s, parameters: %s%n", numberedMethods, numberedMethodParameters);
            System.out.printf("    constructors: %s, parameters: %s%n", constructors, constructorParameters);
        }

        return classes && fields && enumValues
                && namedMethods && numberedMethods && constructors
                && namedMethodParameters && numberedMethodParameters && constructorParameters;
    }
}
