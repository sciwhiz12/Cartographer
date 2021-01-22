package sciwhiz12.cartographer;

import com.google.common.base.Stopwatch;
import sciwhiz12.cartographer.mcp.MCPDatabase;
import sciwhiz12.cartographer.mcp.MCPEntry;
import sciwhiz12.cartographer.srg.SRGDatabase;
import sciwhiz12.cartographer.srg.SRGDatabaseDebuggingWriter;
import sciwhiz12.cartographer.srg.SRGEntry;
import sciwhiz12.cartographer.util.AccessTransformers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class QueryConsole implements Runnable {
    private final SRGDatabase srgDatabase;
    private final MCPDatabase mcpDatabase;
    private final Scanner input;

    public QueryConsole(SRGDatabase srgDatabase, MCPDatabase mcpDatabase, InputStream input) {
        this.srgDatabase = srgDatabase;
        this.mcpDatabase = mcpDatabase;
        this.input = new Scanner(input);
    }

    @Override
    public void run() {
        System.out.println("Query console is active.");
        loop:
        while (true) {
            try {
                System.out.print(" > ");
                String line = input.nextLine();
                List<String> splits = Arrays.asList(line.split(" "));
                switch (splits.get(0)) {
                    case "close", "stop", "quit" -> {
                        break loop;
                    }
                    case "debug_export" -> {
                        if (splits.size() < 2) {
                            System.out.println("Please specify an output file for the debug SRG export.");
                        } else {
                            Path output = Path.of(splits.get(1));
                            System.out.printf("Writing debug SRG export to %s...%n", output);
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            Files.write(output, SRGDatabaseDebuggingWriter.write(srgDatabase), CREATE, TRUNCATE_EXISTING);
                            stopwatch.stop();
                            System.out.printf("Time elapsed for debug export: %s%n", stopwatch.elapsed());
                        }
                    }
                    case "id" -> {
                        int id = Integer.parseInt(splits.get(1));
                        final SRGEntry srgEntry = srgDatabase.getEntryForID(id);
                        if (srgEntry != null) {
                            System.out.printf("Entry for ID %d: %s%n", id, srgEntry);
                            final MCPEntry mcpEntry = mcpDatabase.getEntryForID(id);
                            String mcpName = null;
                            if (mcpEntry != null) {
                                mcpName = mcpEntry.name();
                                System.out.printf("   MCP entry: %s%n", mcpEntry);
                            }
                            String at = AccessTransformers.makeFor(srgEntry, mcpName);
                            if (at != null)
                                System.out.printf("   Access Transformer: %s%n", at);
                        } else
                            System.out.println("No entry found for that ID!");
                    }
                    default -> System.out.println("Unknown command.");
                }
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error while parsing input from console: " + e);
            }
        }
    }
}
