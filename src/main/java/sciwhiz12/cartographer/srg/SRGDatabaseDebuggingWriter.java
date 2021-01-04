package sciwhiz12.cartographer.srg;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

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

    @VisibleForTesting
    public static List<String> write(SRGDatabase db) {
        @SuppressWarnings("UnstableApiUsage")
        ImmutableList.Builder<String> output = ImmutableList.builderWithExpectedSize(50000);

        final var classToFields = Multimaps
                .index(db.fields().values(), SRGEntry.ClassMember::parentClass);
        final var classToNamedMethods = Multimaps
                .index(db.namedMethods().values(), SRGEntry.ClassMember::parentClass);
        final var classToNumberedMethods = Multimaps
                .index(db.numberedMethods().values(), SRGEntry.ClassMember::parentClass);
        final var classToConstructors = Multimaps
                .index(db.constructors().values(), SRGEntry.ClassMember::parentClass);

        db.classes().forEach((className, classEntry) -> {
            output.add(format(CLASS_WRITER, classEntry.srgName(), classEntry.reobfName()));
            classToFields
                    .get(classEntry)
                    .forEach(field -> output.add(INDENTATION.repeat(1) + format(FIELD_WRITER, field.srgID(), field.srgName(),
                            field.reobfName())));
            db.enumValues()
                    .get(classEntry)
                    .forEach(enumVal -> output
                            .add(INDENTATION.repeat(1) + format(ENUM_WRITER, enumVal.valueName(), enumVal.reobfName())));
            classToNumberedMethods
                    .get(classEntry)
                    .forEach(method -> {
                        output.add(INDENTATION.repeat(1) + format(NUMBERED_METHOD_WRITER, method.srgID(),
                                method.reobfName(), method.srgName(), method.methodSignature(),
                                method.isStatic()));
                        final String params = db.numberedMethodParameters().get(method).stream()
                                .map(SRGEntry.MethodParameter::index)
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                        if (!params.isEmpty())
                            output.add(INDENTATION.repeat(2) + format(METHOD_PARAMETER_WRITER, params));
                    });
            classToNamedMethods
                    .get(classEntry)
                    .forEach(method -> {
                        output.add(INDENTATION.repeat(1) + format(NAMED_METHOD_WRITER, method.deobfName(), method.reobfName(),
                                method.methodSignature()));
                        final String params = db.namedMethodParameters().get(method).stream()
                                .map(SRGEntry.MethodParameter::index)
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                        if (!params.isEmpty())
                            output.add(INDENTATION.repeat(2) + format(METHOD_PARAMETER_WRITER, params));
                    });
            classToConstructors
                    .get(classEntry)
                    .forEach(constructor -> {
                        output.add(INDENTATION.repeat(1) + format(CONSTRUCTOR_WRITER, constructor.srgID(),
                                constructor.methodSignature()));
                        final String params = db.constructorParameters().get(constructor).stream()
                                .map(SRGEntry.MethodParameter::index)
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                        if (!params.isEmpty())
                            output.add(INDENTATION.repeat(2) + format(METHOD_PARAMETER_WRITER, params));
                    });
        });

        return output.build();
    }
}
