package tk.sciwhiz12.cartographer.mcp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;

public record MCPDatabase(
    ImmutableMap<Integer, MCPEntry.Field> fields,
    ImmutableMap<Integer, MCPEntry.Method> methods,
    ImmutableMultimap<Integer, MCPEntry.Parameter> parameters
) {
    public static MCPDatabase parse(List<String> fieldsLines, List<String> methodsLines, List<String> paramsLines) {
        return MCPParser.read(fieldsLines, methodsLines, paramsLines);
    }

    @Nullable
    public MCPEntry getEntryForID(int srgID) {
        Integer boxedID = srgID;
        if (fields.containsKey(boxedID)) { return fields.get(srgID); }
        if (methods.containsKey(boxedID)) { return methods.get(srgID); }
        return null;
    }

    @NonNull
    public List<MCPEntry.Parameter> getParametersForMethod(int srgID) {
        Integer boxedID = srgID;
        if (parameters.containsKey(srgID)) return parameters.get(srgID).asList();
        return Collections.emptyList();
    }
}
