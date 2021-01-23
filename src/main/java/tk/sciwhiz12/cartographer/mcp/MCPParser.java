package tk.sciwhiz12.cartographer.mcp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static java.lang.Integer.parseInt;
import static tk.sciwhiz12.cartographer.util.Logging.logf;
import static tk.sciwhiz12.cartographer.util.Patterns.CSV;
import static tk.sciwhiz12.cartographer.util.Patterns.PARAM_CSV;

class MCPParser {
    private MCPParser() {}

    static MCPDatabase read(List<String> fieldsLines, List<String> methodsLines, List<String> paramsLines) {
        final Map<Integer, MCPEntry.Field> fields = Collections.synchronizedMap(new Object2ObjectArrayMap<>(5000));
        final Map<Integer, MCPEntry.Method> methods = Collections.synchronizedMap(new Object2ObjectArrayMap<>(5000));
        final Multimap<Integer, MCPEntry.Parameter> params = Multimaps.synchronizedMultimap(
            MultimapBuilder.hashKeys(5000).arrayListValues(4).build());

        fieldsLines.parallelStream()
            .filter(str -> !str.startsWith("searge"))
            .map(CSV::matcher)
            .filter(Matcher::matches)
            .map(matcher -> new MCPEntry.Field(parseInt(matcher.group(1)), matcher.group(2), MCPEntry.Side.of(matcher.group(3)),
                matcher.group(4)))
            .forEach(field -> fields.put(field.srgID(), field));

        methodsLines.parallelStream()
            .filter(str -> !str.startsWith("searge"))
            .map(CSV::matcher)
            .filter(Matcher::matches)
            .map(
                matcher -> new MCPEntry.Method(parseInt(matcher.group(1)), matcher.group(2), MCPEntry.Side.of(matcher.group(3)),
                    matcher.group(4)))
            .forEach(method -> methods.put(method.srgID(), method));

        paramsLines.parallelStream()
            .filter(str -> !str.startsWith("param"))
            .map(PARAM_CSV::matcher)
            .filter(Matcher::matches)
            .map(matcher -> matcher.group(1).isBlank() ?
                new MCPEntry.MethodParameter(parseInt(matcher.group(2)), parseInt(matcher.group(3)),
                    matcher.group(4), MCPEntry.Side.of(matcher.group(5))) :
                new MCPEntry.ConstructorParameter(parseInt(matcher.group(2)), parseInt(matcher.group(3)),
                    matcher.group(4), MCPEntry.Side.of(matcher.group(5))))
            .forEach(param -> params.put(param.methodSrgID(), param));

        logf(" === MCP Import === %n");
        logf("Fields: %d%n", fields.size());
        logf("Methods: %d%n", methods.size());
        logf("Parameters: %d%n", params.size());
        logf(" === === == === === %n");

        return new MCPDatabase(
            ImmutableMap.copyOf(fields),
            ImmutableMap.copyOf(methods),
            ImmutableMultimap.copyOf(params)
        );
    }
}
