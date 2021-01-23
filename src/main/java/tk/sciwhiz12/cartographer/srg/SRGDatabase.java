package tk.sciwhiz12.cartographer.srg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static tk.sciwhiz12.cartographer.srg.SRGEntry.*;

public record SRGDatabase(
    ImmutableMap<String, SRGEntry.Class> classes, // SRG FQ Class Name -> Class (guaranteed to be unique)
    ImmutableMap<Integer, Field> fields, // SRG ID -> Field
    ImmutableMultimap<SRGEntry.Class, EnumValue> enumValues, // Class -> Enum Values
    ImmutableTable<Integer, SRGEntry.Class, NumberedMethod> numberedMethods, // Method SRG ID -> Numbered Method
    ImmutableMultimap<String, NamedMethod> namedMethods, // Method Name -> Named Methods
    ImmutableMap<Integer, Constructor> constructors, // SRG ID -> Constructor
    ImmutableMultimap<NumberedMethod, MethodParameter> numberedMethodParameters, // NumberedMethod -> Parameters
    ImmutableMultimap<NamedMethod, MethodParameter> namedMethodParameters, // NamedMethod -> Parameters
    ImmutableMultimap<Constructor, MethodParameter> constructorParameters // Constructor -> Parameters
) {
    public static SRGDatabase parse(List<String> tsrgLines, List<String> staticsLines, List<String> constructorsLines) {
        return SRGParser.read(tsrgLines, staticsLines, constructorsLines);
    }

    public static SRGDatabase deserialize(List<String> writtenDB) {
        return SRGDatabaseCodec.read(writtenDB);
    }

    // TODO: move this to return a collection instead, for the numbered methods
    @Nullable
    public SRGEntry getEntryForID(int srgID) {
        Integer boxedID = srgID;
        if (fields.containsKey(boxedID)) { return fields.get(srgID); }
        if (numberedMethods.containsRow(boxedID)) {
            return numberedMethods.rowMap().get(srgID).values().stream().findFirst().orElseThrow();
        }
        if (constructors.containsKey(boxedID)) { return constructors.get(srgID); }
        return null;
    }

    @NonNull
    public List<MethodParameter> getParametersForMethod(int srgID) {
        Integer boxedID = srgID;
        final SRGEntry entry = getEntryForID(srgID);
        if (entry instanceof NumberedMethod) {
            return numberedMethodParameters.get((NumberedMethod) entry).asList();
        } else if (entry instanceof NamedMethod) {
            return namedMethodParameters.get((NamedMethod) entry).asList();
        }
        return Collections.emptyList();
    }

    public List<String> serialize() {
        return SRGDatabaseCodec.write(this);
    }

    public void printStatistics(PrintStream out) {
        out.printf("Classes: %d%n", classes().size());
        out.printf("Total fields: %d [ fields: %d, enum values: %d ]%n",
            fields().size() + enumValues().size(), fields().size(), enumValues().size());
        out.printf("Total methods: %d [ numbered: %d, named: %d ]%n",
            numberedMethods().size() + namedMethods().size(), numberedMethods().size(), namedMethods().size());
        out.printf("Total parameters: %d [ numbered: %d, named: %d ]%n",
            numberedMethodParameters().size() + namedMethodParameters().size(), numberedMethodParameters().size(),
            namedMethodParameters().size());
        out.printf("Constructors: %d [ parameters count: %d ]%n", constructors().size(), constructorParameters().size());
    }
}
