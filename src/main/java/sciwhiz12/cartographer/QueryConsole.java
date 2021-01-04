package sciwhiz12.cartographer;

import sciwhiz12.cartographer.mcp.MCPDatabase;
import sciwhiz12.cartographer.mcp.MCPEntry;
import sciwhiz12.cartographer.srg.SRGDatabase;
import sciwhiz12.cartographer.srg.SRGEntry;
import sciwhiz12.cartographer.util.AccessTransformers;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
                    case "close":
                    case "stop":
                    case "quit":
                        break loop;
                    case "id": {
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
                        break;
                    }
                    case "class": {

                        break;
                    }
                    default:
                        System.out.println("Unknown command.");
                }
                System.out.println();
            }
            catch (Exception e) {
                System.err.println("Error while parsing input from console: " + e);
            }
        }
    }
}
