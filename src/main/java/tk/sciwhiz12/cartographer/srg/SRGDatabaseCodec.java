package tk.sciwhiz12.cartographer.srg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static tk.sciwhiz12.cartographer.srg.SRGDatabaseCodec.CodecConstants.*;

class SRGDatabaseCodec {
    @VisibleForTesting
    public static <T extends Number> List<String> write(SRGDatabase db) {
        int size = db.classes().size() + db.fields().size() + db.enumValues().size() + db.namedMethods().size() + db
            .numberedMethods().size() + db.constructors().size() + db.namedMethodParameters().size() + db
            .numberedMethodParameters().size() + db.constructorParameters().size() + 50;
        @SuppressWarnings("UnstableApiUsage")
        ImmutableList.Builder<String> output = ImmutableList.builderWithExpectedSize(size);

        output.add(CLASS_HEADER);
        output.addAll(
            db.classes().values()
                .stream()
                .map(clz -> CLASS_WRITER.formatted(
                    clz.reobfName(),
                    clz.srgName()
                ))
                .collect(Collectors.toList())
        );
        output.add(CLASS_FOOTER);

        output.add(FIELD_HEADER);
        output.addAll(
            db.fields().values()
                .stream()
                .map(field -> FIELD_WRITER.formatted(
                    field.srgID(),
                    field.reobfName(),
                    field.srgName(),
                    field.parentClass().srgName()
                ))
                .collect(Collectors.toList())
        );
        output.add(FIELD_FOOTER);

        output.add(ENUM_HEADER);
        output.addAll(
            db.enumValues().values()
                .stream()
                .map(enumValue -> ENUM_WRITER.formatted(
                    enumValue.reobfName(),
                    enumValue.valueName(),
                    enumValue.parentClass().srgName()
                ))
                .collect(Collectors.toList())
        );
        output.add(ENUM_FOOTER);

        output.add(NAMED_METHOD_HEADER);
        output.addAll(
            db.namedMethods().values()
                .stream()
                .map(namedMethod -> NAMED_METHOD_WRITER.formatted(
                    namedMethod.reobfName(),
                    namedMethod.deobfName(),
                    namedMethod.parentClass().srgName(),
                    namedMethod.methodSignature()
                ))
                .collect(Collectors.toList())
        );
        output.add(NAMED_METHOD_FOOTER);

        output.add(NUMBERED_METHOD_HEADER);
        output.addAll(
            db.numberedMethods().values()
                .stream()
                .map(numberedMethod -> NUMBERED_METHOD_WRITER.formatted(
                    numberedMethod.srgID(),
                    numberedMethod.reobfName(),
                    numberedMethod.srgName(),
                    numberedMethod.parentClass().srgName(),
                    numberedMethod.methodSignature(),
                    numberedMethod.isStatic()
                ))
                .collect(Collectors.toList())
        );
        output.add(NUMBERED_METHOD_FOOTER);

        output.add(CONSTRUCTOR_HEADER);
        output.addAll(
            db.constructors().values()
                .stream()
                .map(constructor -> CONSTRUCTOR_WRITER.formatted(
                    constructor.srgID(),
                    constructor.parentClass().srgName(),
                    constructor.methodSignature()
                ))
                .collect(Collectors.toList())
        );
        output.add(CONSTRUCTOR_FOOTER);

        output.add(NAMED_METHOD_PARAMETER_HEADER);
        output.addAll(
            db.namedMethodParameters().entries()
                .stream()
                .map(entry -> NAMED_METHOD_PARAMETER_WRITER.formatted(
                    entry.getKey().parentClass().srgName(),
                    entry.getKey().deobfName(),
                    entry.getKey().methodSignature(),
                    entry.getValue().index()
                ))
                .collect(Collectors.toList())
        );
        output.add(NAMED_METHOD_PARAMETER_FOOTER);

        output.add(NUMBERED_METHOD_PARAMETER_HEADER);
        output.addAll(
            db.numberedMethodParameters().entries()
                .stream()
                .map(entry -> NUMBERED_METHOD_PARAMETER_WRITER.formatted(
                    entry.getKey().srgID(),
                    entry.getValue().index()
                ))
                .collect(Collectors.toList())
        );
        output.add(NUMBERED_METHOD_PARAMETER_FOOTER);

        output.add(CONSTRUCTOR_PARAMETER_HEADER);
        output.addAll(
            db.constructorParameters().entries()
                .stream()
                .map(entry -> CONSTRUCTOR_PARAMETER_WRITER.formatted(
                    entry.getKey().srgID(),
                    entry.getValue().index()
                ))
                .collect(Collectors.toList())
        );
        output.add(CONSTRUCTOR_PARAMETER_FOOTER);

        return output.build();
    }

    static SRGDatabase read(final List<String> strings) {

        final ImmutableMap<String, SRGEntry.Class> classes = strings.stream()
            .dropWhile(str -> !CLASS_HEADER.contentEquals(str))
            .takeWhile(str -> !CLASS_FOOTER.contentEquals(str))
            .map(str -> CLASS_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.Class(
                matcher.group("srg"),
                matcher.group("reobf")
            ))
            .collect(toImmutableMap(SRGEntry.Class::srgName, identity()));

        final ImmutableMap<Integer, SRGEntry.Field> fields = strings.stream()
            .dropWhile(str -> !FIELD_HEADER.contentEquals(str))
            .takeWhile(str -> !FIELD_FOOTER.contentEquals(str))
            .map(str -> FIELD_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.Field(
                parseInt(matcher.group("id")),
                matcher.group("srg"),
                matcher.group("reobf"),
                classes.get(matcher.group("class"))
            ))
            .collect(toImmutableMap(SRGEntry.Field::srgID, identity()));

        final ImmutableMultimap<SRGEntry.Class, SRGEntry.EnumValue> enumValues = strings.stream()
            .dropWhile(str -> !ENUM_HEADER.contentEquals(str))
            .takeWhile(str -> !ENUM_FOOTER.contentEquals(str))
            .map(str -> ENUM_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.EnumValue(
                matcher.group("value"),
                matcher.group("reobf"),
                classes.get(matcher.group("class"))
            ))
            .collect(toImmutableMultimap(
                SRGEntry.EnumValue::parentClass,
                identity()
            ));

        final ImmutableMultimap<String, SRGEntry.NamedMethod> namedMethods = strings.stream()
            .dropWhile(str -> !NAMED_METHOD_HEADER.contentEquals(str))
            .takeWhile(str -> !NAMED_METHOD_FOOTER.contentEquals(str))
            .map(str -> NAMED_METHOD_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.NamedMethod(
                matcher.group("deobf"),
                matcher.group("reobf"),
                classes.get(matcher.group("class")),
                matcher.group("signature")
            ))
            .collect(toImmutableMultimap(
                SRGEntry.NamedMethod::deobfName,
                identity()
            ));

        final ImmutableTable<Integer, SRGEntry.Class, SRGEntry.NumberedMethod> numberedMethods = strings.stream()
            .dropWhile(str -> !NUMBERED_METHOD_HEADER.contentEquals(str))
            .takeWhile(str -> !NUMBERED_METHOD_FOOTER.contentEquals(str))
            .map(str -> NUMBERED_METHOD_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.NumberedMethod(
                parseInt(matcher.group("id")),
                matcher.group("srg"),
                matcher.group("reobf"),
                classes.get(matcher.group("class")),
                matcher.group("signature"),
                parseBoolean(matcher.group("static"))
            ))
            .collect(ImmutableTable
                .toImmutableTable(SRGEntry.NumberedMethod::srgID, SRGEntry.NumberedMethod::parentClass, identity()));

        final ImmutableMap<Integer, SRGEntry.Constructor> constructors = strings.stream()
            .dropWhile(str -> !CONSTRUCTOR_HEADER.contentEquals(str))
            .takeWhile(str -> !CONSTRUCTOR_FOOTER.contentEquals(str))
            .map(str -> CONSTRUCTOR_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.Constructor(
                parseInt(matcher.group("id")),
                classes.get(matcher.group("class")),
                matcher.group("signature")
            ))
            .collect(toImmutableMap(SRGEntry.Constructor::srgID, identity()));

        final ImmutableMultimap<SRGEntry.NamedMethod, SRGEntry.MethodParameter> namedMethodParameters = strings.stream()
            .dropWhile(str -> !NAMED_METHOD_PARAMETER_HEADER.contentEquals(str))
            .takeWhile(str -> !NAMED_METHOD_PARAMETER_FOOTER.contentEquals(str))
            .map(str -> NAMED_METHOD_PARAMETER_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> {
                final SRGEntry.Class classEntry = classes.get(matcher.group("class"));
                final String methodName = matcher.group("methodName");
                final String methodSignature = matcher.group("methodSignature");
                return new SRGEntry.MethodParameter(
                    namedMethods.get(methodName)
                        .stream()
                        .filter(method -> method.parentClass().equals(classEntry))
                        .filter(method -> method.methodSignature().equals(methodSignature))
                        .findFirst().orElseThrow(IllegalStateException::new),
                    parseInt(matcher.group("index"))
                );
            })
            .collect(toImmutableMultimap(
                entry -> (SRGEntry.NamedMethod) entry.parentMethod(),
                identity()
            ));

        final ImmutableMultimap<SRGEntry.NumberedMethod, SRGEntry.MethodParameter> numberedMethodParameters = strings.stream()
            .dropWhile(str -> !NUMBERED_METHOD_PARAMETER_HEADER.contentEquals(str))
            .takeWhile(str -> !NUMBERED_METHOD_PARAMETER_FOOTER.contentEquals(str))
            .map(str -> NUMBERED_METHOD_PARAMETER_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.MethodParameter(
                numberedMethods.row(parseInt(matcher.group("methodId"))).values().stream().findFirst().orElseThrow(),
                parseInt(matcher.group("index"))
            ))
            .collect(toImmutableMultimap(
                entry -> (SRGEntry.NumberedMethod) entry.parentMethod(),
                identity()
            ));

        final ImmutableMultimap<SRGEntry.Constructor, SRGEntry.MethodParameter> constructorParameters = strings.stream()
            .dropWhile(str -> !CONSTRUCTOR_PARAMETER_HEADER.contentEquals(str))
            .takeWhile(str -> !CONSTRUCTOR_PARAMETER_FOOTER.contentEquals(str))
            .map(str -> CONSTRUCTOR_PARAMETER_WRITER.regex().matcher(str))
            .filter(Matcher::matches)
            .map(matcher -> new SRGEntry.MethodParameter(
                constructors.get(parseInt(matcher.group("constructorId"))),
                parseInt(matcher.group("index"))
            ))
            .collect(toImmutableMultimap(
                entry -> (SRGEntry.Constructor) entry.parentMethod(),
                identity()
            ));

        return new SRGDatabase(
            classes,
            fields,
            enumValues,
            numberedMethods,
            namedMethods,
            constructors,
            numberedMethodParameters,
            namedMethodParameters,
            constructorParameters
        );
    }

    record Line(String format, Pattern regex) {
        public Line(String format, String regex) {
            this(format, Pattern.compile(regex));
        }

        public String formatted(Object... args) {
            return format().formatted(args);
        }
    }

    /*
     * #%L
     * guava-stream
     * %%
     * Copyright (C) 2015 Edson Yanaga
     * %%
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     * #L%
     *
     * Modified to inline both methods used and compacted (MoreCollectors.toImmutableMap)
     */
    private static <T, K, V, B extends ImmutableMultimap.Builder<K, V>, M extends ImmutableMultimap<K, V>> Collector<T, ?, M> toImmutableMultimap(
        Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends V> valueMapper) {
        //noinspection unchecked
        return Collector.of(
            ImmutableMultimap::builder,
            (objectBuilder, t) -> objectBuilder.put(keyMapper.apply(t), valueMapper.apply(t)),
            (objectBuilder, objectBuilder2) -> objectBuilder.putAll(objectBuilder2.build()),
            tBuilder -> (M) tBuilder.build(),
            UNORDERED, CONCURRENT);
    }

    static final class CodecConstants {
        static String CLASS_HEADER = "{ [START] Class }";
        static Line CLASS_WRITER = new Line(
            "class reobf:%s srg:%s",
            "^class reobf:(?<reobf>.+?) srg:(?<srg>.+)$");
        static String CLASS_FOOTER = "{ [END] Class }";

        static String FIELD_HEADER = "{ [START] Field }";
        static Line FIELD_WRITER = new Line(
            "field id:%d reobf:%s srg:%s class:%s",
            "^field id:(?<id>\\d+) reobf:(?<reobf>.+?) srg:(?<srg>.+?) class:(?<class>.+)$");
        static String FIELD_FOOTER = "{ [END] Field }";

        static String ENUM_HEADER = "{ [START] Enum }";
        static Line ENUM_WRITER = new Line(
            "enum reobf:%s value:%s class:%s",
            "^enum reobf:(?<reobf>.+?) value:(?<value>.+?) class:(?<class>.+)$");
        static String ENUM_FOOTER = "{ [END] Enum }";

        static String NAMED_METHOD_HEADER = "{ [START] NamedMethod }";
        static Line NAMED_METHOD_WRITER = new Line(
            "method reobf:%s deobf:%s class:%s signature:%s",
            "^method reobf:(?<reobf>.+?) deobf:(?<deobf>.+?) class:(?<class>.+?) signature:(?<signature>.+)$");
        static String NAMED_METHOD_FOOTER = "{ [END] NamedMethod }";

        static String NUMBERED_METHOD_HEADER = "{ [START] NumberedMethod }";
        static Line NUMBERED_METHOD_WRITER = new Line(
            "method id:%d reobf:%s srg:%s class:%s signature:%s static:%s",
            "^method id:(?<id>\\d+) reobf:(?<reobf>.+?) srg:(?<srg>.+?) class:(?<class>.+?) signature:(?<signature>.+) " +
                "static:(?<static>true|false)$");
        static String NUMBERED_METHOD_FOOTER = "{ [END] Class }";

        static String CONSTRUCTOR_HEADER = "{ [START] Constructor }";
        static Line CONSTRUCTOR_WRITER = new Line(
            "constructor id:%d class:%s signature:%s",
            "^constructor id:(?<id>\\d+) class:(?<class>.+?) signature:(?<signature>.+)$");
        static String CONSTRUCTOR_FOOTER = "{ [END] Constructor }";

        static String NAMED_METHOD_PARAMETER_HEADER = "{ [START] Parameter: NamedMethod }";
        static Line NAMED_METHOD_PARAMETER_WRITER = new Line(
            "parameter class:%s method_name:%s method_signature:%s index:%d",
            "^parameter class:(?<class>.+?) method_name:(?<methodName>.+?) method_signature:(?<methodSignature>.+?) " +
                "index: (?<index>\\d+)$");
        static String NAMED_METHOD_PARAMETER_FOOTER = "{ [END] Parameter: NamedMethod }";

        static String NUMBERED_METHOD_PARAMETER_HEADER = "{ [START] Parameter: NumberedMethod }";
        static Line NUMBERED_METHOD_PARAMETER_WRITER = new Line(
            "parameter method_id:%d index:%d",
            "^parameter method_id:(?<methodId>\\d+) index:(?<index>\\d+)$");
        static String NUMBERED_METHOD_PARAMETER_FOOTER = "{ [END] Parameter: NumberedMethod }";

        static String CONSTRUCTOR_PARAMETER_HEADER = "{ [START] Parameter: Constructor }";
        static Line CONSTRUCTOR_PARAMETER_WRITER = new Line(
            "parameter constructor_id:%d index:%d",
            "^parameter constructor_id:(?<constructorId>\\d+) index:(?<index>\\d+)$");
        static String CONSTRUCTOR_PARAMETER_FOOTER = "{ [END] Parameter: Constructor }";
    }
}
