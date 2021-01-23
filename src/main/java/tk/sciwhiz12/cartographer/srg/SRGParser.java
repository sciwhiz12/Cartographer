package tk.sciwhiz12.cartographer.srg;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import static java.lang.Integer.parseInt;
import static java.util.regex.Matcher.quoteReplacement;
import static tk.sciwhiz12.cartographer.util.Logging.*;
import static tk.sciwhiz12.cartographer.util.Patterns.*;

class SRGParser {
    static final ImmutableMap<Character, Integer> charMap = ImmutableMap.<Character, Integer>builder()
        .put('B', 1).put('C', 1).put('F', 1).put('S', 1).put('Z', 1).put('I', 1).put('L', 1)
        .put('D', 2).put('J', 2).build();

    private SRGParser() {}

    static SRGDatabase read(List<String> tsrgLines, List<String> staticsLines, List<String> constructorsLines) {
        IntList staticList = parseStaticMethods(staticsLines);

        Map<String, SRGEntry.Class> classes = new Object2ObjectArrayMap<>(5000);
        Int2ObjectMap<SRGEntry.Field> fields = new Int2ObjectArrayMap<>(5000);
        Multimap<SRGEntry.Class, SRGEntry.EnumValue> enumValues = MultimapBuilder.hashKeys(5000).arrayListValues(4).build();
        Table<Integer, SRGEntry.Class, SRGEntry.NumberedMethod> numbered = HashBasedTable.create(5000, 1000);
        Multimap<String, SRGEntry.NamedMethod> named = MultimapBuilder.hashKeys(5000).arrayListValues(2).build();

        int errCount = 0;
        SRGEntry.Class lastKnownClass = null;
        for (int lineNum = 0; lineNum < tsrgLines.size(); lineNum++) {
            String line = tsrgLines.get(lineNum);
            Matcher classMatcher = TSRG_CLASS_HEADER.matcher(line); // Class matching
            if (classMatcher.matches()) {
                String reobfName = classMatcher.group("reobf");
                String deobfName = classMatcher.group("srg");
                lastKnownClass = new SRGEntry.Class(deobfName, reobfName);
                debugf("L%d - CLASS header: %s%n", lineNum + 1, lastKnownClass);
                classes.put(deobfName, lastKnownClass);
                continue;
            }
            if (lastKnownClass == null) { throw new RuntimeException("No class in first line?"); }
            Matcher fieldMatcher = TSRG_FIELD.matcher(line); // Field matching
            if (fieldMatcher.matches()) {
                String reobfName = fieldMatcher.group("reobf");
                String originalSrgName = fieldMatcher.group("srg");
                int srgID = parseInt(fieldMatcher.group("id"));
                SRGEntry.Field field = new SRGEntry.Field(srgID, originalSrgName, reobfName, lastKnownClass);
                debugf("L%d - Field: %s%n", lineNum + 1, field);
                fields.put(srgID, field);
                continue;
            }
            Matcher enumMatcher = TSRG_ENUM_VALUE.matcher(line); // Enum value matching
            if (enumMatcher.matches()) {
                String reobfName = enumMatcher.group("reobf");
                String valueName = enumMatcher.group("value");
                SRGEntry.EnumValue enumValue = new SRGEntry.EnumValue(valueName, reobfName, lastKnownClass);
                debugf("L%s - Enum value: %s%n", lineNum + 1, enumValue);
                enumValues.put(lastKnownClass, enumValue);
                continue;
            }
            Matcher numberedMatcher = TSRG_NUMBERED_METHOD.matcher(line); // Numbered (srg-id) method matching
            if (numberedMatcher.matches()) {
                String reobfName = numberedMatcher.group("reobf");
                String methodSignature = numberedMatcher.group("signature");
                String originalSrgName = numberedMatcher.group("srg");
                int srgID = parseInt(numberedMatcher.group("id"));
                boolean isStatic = staticList.contains(srgID);
                SRGEntry.NumberedMethod method = new SRGEntry.NumberedMethod(srgID, originalSrgName, reobfName, lastKnownClass,
                    methodSignature, isStatic);
                debugf("L%d - Numbered method: %s%n", lineNum + 1, method);
                numbered.put(srgID, lastKnownClass, method);
                continue;
            }
            Matcher namedMatcher = TSRG_NAMED_METHOD.matcher(line); // Named method matching
            if (namedMatcher.matches()) {
                String reobfName = namedMatcher.group("reobf");
                String methodSignature = namedMatcher.group("signature");
                String deobfName = namedMatcher.group("deobf");
                SRGEntry.NamedMethod method = new SRGEntry.NamedMethod(deobfName, reobfName, lastKnownClass,
                    methodSignature);
                debugf("L%d - Named method: %s%n", lineNum + 1, method);
                named.put(deobfName, method);
                continue;
            }
            errf("!!! L%d is not recognizable!: %s%n", lineNum + 1, line);
            errCount++;
        }

        final Int2ObjectMap<SRGEntry.Constructor> constructors = parseConstructors(classes, constructorsLines);
        final ImmutableMap<String, SRGEntry.Class> reobfClassesMap = Maps
            .uniqueIndex(classes.values(), SRGEntry.Class::reobfName);

        //noinspection UnstableApiUsage
        numbered = ImmutableTable.copyOf(Tables.transformValues(numbered, obfSignaturesTransformer(
            reobfClassesMap,
            (orig, newSig) -> new SRGEntry.NumberedMethod(
                orig.srgID(),
                orig.srgName(),
                orig.reobfName(),
                orig.parentClass(),
                newSig,
                orig.isStatic())
            )
        ));

        named = ImmutableMultimap.copyOf(Multimaps.transformValues(named, obfSignaturesTransformer(
            reobfClassesMap,
            (orig, newSig) -> new SRGEntry.NamedMethod(
                orig.deobfName(),
                orig.reobfName(),
                orig.parentClass(),
                newSig)
            )
        ));

        final Multimap<SRGEntry.NumberedMethod, SRGEntry.MethodParameter> numberedParams = createParameters(numbered.values());
        final Multimap<SRGEntry.NamedMethod, SRGEntry.MethodParameter> namedParams = createParameters(named.values());
        final Multimap<SRGEntry.Constructor, SRGEntry.MethodParameter> constructorParams = createParameters(
            constructors.values());

        final SRGDatabase srgDatabase = new SRGDatabase(
            ImmutableMap.copyOf(classes),
            ImmutableMap.copyOf(fields),
            ImmutableMultimap.copyOf(enumValues),
            ImmutableTable.copyOf(numbered),
            ImmutableMultimap.copyOf(named),
            ImmutableMap.copyOf(constructors),
            ImmutableMultimap.copyOf(numberedParams),
            ImmutableMultimap.copyOf(namedParams),
            ImmutableMultimap.copyOf(constructorParams));

        logf(" === SRG Import === %n");
        srgDatabase.printStatistics(System.out);
        logf("Number of errors: %d%n", errCount);
        logf(" === === == === === %n");

        return srgDatabase;
    }

    @SuppressWarnings("Guava")
    static <M extends SRGEntry.Method> com.google.common.base.Function<M, M> obfSignaturesTransformer(
        final Map<String, SRGEntry.Class> classes,
        final SignatureModifier<M> modifier
    ) {
        return method -> {
            if (method == null) return null;
            Matcher matcher = METHOD_DESCRIPTOR__REFERENCE_TYPE.matcher(method.methodSignature());
            debugf("Deobfuscating method signature: %s%n", method.methodSignature());
            String newSignature = matcher.replaceAll(match -> {
                String name = match.group(1);
                if (!name.contains("/") && name.chars().noneMatch(Character::isUpperCase)) {
                    SRGEntry.Class deobfClass = classes.get(name);
                    if (deobfClass != null) return 'L' + quoteReplacement(deobfClass.srgName()) + ';';
                    errf("DEOBF_SIGNATURE: Cannot find deobf class name for %s for %s%n", name, method);
                }
                return 'L' + quoteReplacement(name) + ';';
            });
            debugf("Deobfuscated signature: %s%n", newSignature);
            return modifier.create(method, newSignature);
        };
    }

    interface SignatureModifier<M extends SRGEntry.Method> {
        M create(M original, String newMethodSignature);
    }

    static <T extends SRGEntry.Method> ImmutableMultimap<T, SRGEntry.MethodParameter> createParameters(Collection<T> methods) {
        ImmutableMultimap.Builder<T, SRGEntry.MethodParameter> parameters = ImmutableMultimap.builder();
        for (T method : methods) {
            final Matcher signatureMatcher = METHOD_SIGNATURE_PARAMETERS.matcher(method.methodSignature());
            if (!signatureMatcher.matches() || signatureMatcher.group("parameters").isEmpty()) continue;
            final Matcher descriptor = METHOD_DESCRIPTOR.matcher(signatureMatcher.group("parameters"));
            boolean isStatic = method instanceof SRGEntry.NumberedMethod && ((SRGEntry.NumberedMethod) method).isStatic();
            // See JVM Spec section 4.3.3 for details of the parameter index/units
            AtomicInteger index = new AtomicInteger(isStatic ? 0 : 1);
            descriptor
                .results()
                .map(match -> match.group(1))
                .map(str -> str.charAt(0))
                .filter(charMap::containsKey)
                .mapToInt(charMap::get)
                .forEach(unit -> parameters.put(method, new SRGEntry.MethodParameter(method, index.getAndAdd(unit))));
        }
        return parameters.build();
    }

    static IntList parseStaticMethods(List<String> lines) {
        IntList staticsList = new IntArrayList();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher lineMatch = SRG_NUMBER.matcher(line);
            if (!lineMatch.matches()) {
                errf("STATICS: L%d does not match naming pattern: %s%n", i + 1, line);
                continue;
            } else if (!lineMatch.group("type").equals("func")) {
                errf("STATICS: L%d is not a function: %s%n", i + 1, line);
                continue;
            }
            staticsList.add(parseInt(lineMatch.group("id")));
        }

        logf("Parsed %d static method declarations%n", staticsList.size());
        return staticsList;
    }

    static Int2ObjectMap<SRGEntry.Constructor> parseConstructors(Map<String, SRGEntry.Class> classes,
        List<String> lines) {
        Int2ObjectMap<SRGEntry.Constructor> constructors = Int2ObjectMaps.synchronize(new Int2ObjectArrayMap<>(1000));

        lines.parallelStream()
            .map(CONSTRUCTOR_ENTRY::matcher)
            .filter(Matcher::matches)
            .filter(matcher -> classes.containsKey(matcher.group("class")))
            .map(lineMatch -> new SRGEntry.Constructor(parseInt(lineMatch.group("id")),
                classes.get(lineMatch.group("class")),
                lineMatch.group("signature")))
            .forEach(constructor -> constructors.put(constructor.srgID(), constructor));

        logf("Found %d valid constructors%n", constructors.size());
        return constructors;
    }
}
