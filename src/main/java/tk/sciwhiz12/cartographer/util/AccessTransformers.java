package tk.sciwhiz12.cartographer.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import tk.sciwhiz12.cartographer.srg.SRGEntry;

public class AccessTransformers {
    private AccessTransformers() {}

    @Nullable
    public static String makeFor(SRGEntry entry, @Nullable String mcpName) {
        if (mcpName != null) mcpName = " # " + mcpName;
        else mcpName = "";
        if (entry instanceof SRGEntry.Class clz) {
            return "public %s".formatted(clz.srgName());
        } else if (entry instanceof SRGEntry.Field field) {
            return "public %s %s".formatted(field.parentClass().srgName().replace('/', '.'), field.srgName()) + mcpName;
        } else if (entry instanceof SRGEntry.NamedMethod method) {
            return "public %s %s%s"
                    .formatted(method.parentClass().srgName().replace('/', '.'), method.deobfName(), method.methodSignature());
        } else if (entry instanceof SRGEntry.NumberedMethod method) {
            return "public %s %s%s".formatted(method.parentClass().srgName().replace('/', '.'), method.srgName(),
                    method.methodSignature()) + mcpName;
        } else if (entry instanceof SRGEntry.Constructor constructor) {
            return "public %s <init>%s"
                    .formatted(constructor.parentClass().srgName().replace('/', '.'), constructor.methodSignature());
        }
        return null;
    }
}
