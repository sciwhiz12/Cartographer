package sciwhiz12.cartographer.util;

import java.util.regex.Pattern;

public final class Patterns {
    public static final Pattern CSV = Pattern.compile("^(?:func|field)_(\\d+)_[$\\w/]+?_??,(.+?),(.+?),(.*)$");
    public static final Pattern PARAM_CSV = Pattern.compile("^p_(i??)(\\d+?)_(\\d+?)_??,(.+?),(\\d)$");

    public static final Pattern TSRG_CLASS_HEADER = Pattern.compile("^(?<reobf>[$\\w/]+) (?<srg>[$\\w/]+)$");
    public static final Pattern TSRG_FIELD = Pattern.compile("^\\s*(?<reobf>[$\\w/]+) (?<srg>field_(?<id>\\d+)_[$\\w/]+?_?)$");
    public static final Pattern TSRG_ENUM_VALUE = Pattern.compile("^\\s*(?<reobf>[$\\w/]+) (?<value>[$\\w/]+)$");
    public static final Pattern TSRG_NUMBERED_METHOD = Pattern.compile("^\\s*(?<reobf>[$\\w/]+?) (?<signature>.+?) (?<srg>func_(?<id>\\d+)_[$\\w/]+?_?)$");
    public static final Pattern TSRG_NAMED_METHOD = Pattern.compile("^\\s*(?<reobf>[$\\w/]+?) (?<signature>.*?) (?<deobf>[$\\w/]+)$");

    public static final Pattern METHOD_DESCRIPTOR__REFERENCE_TYPE = Pattern.compile("L(?<class>.+?);");

    public static final Pattern METHOD_SIGNATURE_PARAMETERS = Pattern.compile("\\((?<parameters>.*)\\).+");
    public static final Pattern METHOD_DESCRIPTOR = Pattern.compile("(?<descriptor>(?:L.*?;)|[SBIJZDF])");

    public static final Pattern SRG_NUMBER = Pattern.compile("(?<type>func|field)_(?<id>\\d+)_(?:[$\\w/]*)_?");
    public static final Pattern CONSTRUCTOR_ENTRY = Pattern.compile("^(?<id>\\d+) (?<class>[$\\w/]+) (?<signature>[\\[();$/\\w]+)$");

    private Patterns() {}
}
