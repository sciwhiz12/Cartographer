package tk.sciwhiz12.cartographer.srg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;

import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Stream.of;

@VisibleForTesting
public class SRGDatabaseDebuggingWriter {
    static final String INDENTATION = "    ";

    static final String CLASS_WRITER = "class: srg = %s, reobf = %s";
    static final String FIELD_WRITER = "field: id = %d, srg = %s, reobf = %s";
    static final String ENUM_WRITER = "enum: value = %s, reobf = %s";
    static final String NAMED_METHOD_WRITER = "method: deobf = %s, reobf = %s, signature = %s";
    static final String NUMBERED_METHOD_WRITER = "method: id = %d, srg = %s, reobf = %s, signature = %s, static = %s";
    static final String CONSTRUCTOR_WRITER = "constructor: id = %d, signature = %s";
    static final String METHOD_PARAMETER_WRITER = "params: indexes = %s";

    private static BinaryOperator<String> joiningStringReduction() {
        return (a, b) -> {
            if (a.isEmpty() && !b.isEmpty()) return b;
            if (b.isEmpty() && !a.isEmpty()) return a;
            return a + ", " + b;
        };
    }

    @VisibleForTesting
    public static List<String> write(SRGDatabase db) {
        final var classToFields = Multimaps
                .index(db.fields().values(), SRGEntry.ClassMember::parentClass);
        final var classToNamedMethods = Multimaps
                .index(db.namedMethods().values(), SRGEntry.ClassMember::parentClass);
        final var classToNumberedMethods = Multimaps
                .index(db.numberedMethods().values(), SRGEntry.ClassMember::parentClass);
        final var classToConstructors = Multimaps
                .index(db.constructors().values(), SRGEntry.ClassMember::parentClass);

        final Iterator<String> lines = db.classes().values().stream()
                .flatMap(classEntry -> stream(

                        of(format(CLASS_WRITER, classEntry.srgName(), classEntry.reobfName())),

                        classToFields.get(classEntry).stream()
                                .map(field -> indent(1)
                                        + format(FIELD_WRITER, field.srgID(), field.srgName(), field.reobfName())),

                        db.enumValues().get(classEntry).stream()
                                .map(enumVal -> indent(1)
                                        + format(ENUM_WRITER, enumVal.valueName(), enumVal.reobfName())),

                        classToNumberedMethods.get(classEntry).stream()
                                .flatMap(method -> stream(

                                        of(indent(1)
                                                + format(NUMBERED_METHOD_WRITER, method.srgID(), method.reobfName(),
                                                method.srgName(), method.methodSignature(), method.isStatic())),

                                        db.numberedMethodParameters().get(method).stream()
                                                .mapToInt(SRGEntry.MethodParameter::index)
                                                .mapToObj(Integer::toString)
                                                .reduce(joiningStringReduction())
                                                .map(params -> indent(2)
                                                        + format(METHOD_PARAMETER_WRITER, params))
                                                .stream()
                                )),

                        classToNamedMethods.get(classEntry).stream()
                                .flatMap(method -> stream(

                                        of(indent(1)
                                                + format(NAMED_METHOD_WRITER, method.deobfName(), method.reobfName(),
                                                method.methodSignature())),

                                        db.namedMethodParameters().get(method).stream()
                                                .mapToInt(SRGEntry.MethodParameter::index)
                                                .mapToObj(Integer::toString)
                                                .reduce(joiningStringReduction())
                                                .map(params -> indent(2)
                                                        + format(METHOD_PARAMETER_WRITER, params))
                                                .stream()
                                )),

                        classToConstructors.get(classEntry).stream()
                                .flatMap(constructor -> stream(

                                        of(indent(1)
                                                + format(CONSTRUCTOR_WRITER, constructor.srgID(),
                                                constructor.methodSignature())),

                                        db.constructorParameters().get(constructor).stream()
                                                .mapToInt(SRGEntry.MethodParameter::index)
                                                .mapToObj(Integer::toString)
                                                .reduce(joiningStringReduction())
                                                .map(params -> indent(2)
                                                        + format(METHOD_PARAMETER_WRITER, params))
                                                .stream()
                                ))
                ))
                .iterator();

        //noinspection UnstableApiUsage
        return ImmutableList.<String>builderWithExpectedSize(100000).addAll(lines).build();
    }

    @SafeVarargs
    private static <I> Stream<I> stream(Stream<? extends I>... streams) {
        return of(streams).flatMap(Function.identity());
    }

    private static String indent(int count) {
        return INDENTATION.repeat(count);
    }
}
