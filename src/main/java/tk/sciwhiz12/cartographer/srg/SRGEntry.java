package tk.sciwhiz12.cartographer.srg;

public interface SRGEntry {
    interface HasID extends SRGEntry {
        int srgID();
    }

    interface ClassMember extends SRGEntry {
        Class parentClass();
    }

    interface Method extends ClassMember {
        String methodSignature();
    }

    interface HasSrgName {
        String srgName();
    }

    record Class(String srgName, String reobfName) implements SRGEntry, HasSrgName {}

    record EnumValue(String valueName, String reobfName, Class parentClass) implements ClassMember {}

    record Field(int srgID, String srgName, String reobfName, Class parentClass)
        implements HasID, ClassMember, HasSrgName {}

    record NamedMethod(String deobfName, String reobfName, Class parentClass, String methodSignature)
        implements Method {}

    record NumberedMethod(int srgID, String srgName, String reobfName, Class parentClass, String methodSignature,
                          boolean isStatic)
        implements HasID, Method, HasSrgName {}

    record Constructor(int srgID, Class parentClass, String methodSignature) implements HasID, Method {}

    record MethodParameter(Method parentMethod, int index) implements SRGEntry {}
}
