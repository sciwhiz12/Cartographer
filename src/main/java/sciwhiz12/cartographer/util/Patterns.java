package sciwhiz12.cartographer.util;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public final class Patterns {
    /*
      ^                                                    - match start of string
       (?:func|field)                                      - match either literal 'func' or 'field' {non-capturing group}
                     _                                     - match literal '_' (underscore)
                      (\d+)                                - match 1 or more digits {group 1}
                           _                               - match literal '_' (underscore)
                            [$\w/]+?                       - lazily match 1 or more of [$ (dollar sign), any word character,
                                                                                        / (forward slash)]
                                    _??                    - lazily match 0 or 1 of literal '_' (underscore)
                                       ,                   - match literal ',' (comma)
                                        (.+?)              - lazily match 1 or more of any character except newline {group 2}
                                             ,             - match literal ',' (comma)
                                              (.+?)        - lazily match 1 or more of any character except newline {group 3}
                                                   ,       - match literal ',' (comma)
                                                    (.*)   - match 0 or more of any character except newline {group 4}
                                                        $  - match end of string
     */
    public static final Pattern CSV = compile("^(?:func|field)_(\\d+)_[$\\w/]+?_??,(.+?),(.+?),(.*)$");
    /*
      ^                                     - match start of string
       p_                                   - match literal 'p_'
         (i??)                              - lazily match 0 or 1 of literal 'i' {group 1}
              (\d+?)                        - lazily match 1 or more digits {group 2}
                    _                       - match literal '_' (underscore)
                     (\d+?)                 - lazily match 1 or more digits {group 3}
                           _??              - lazily match 0 or 1 of literal '_' (underscore)
                              ,             - match literal ',' (comma)
                               (.+?)        - lazily match 1 or more of any character except newline {group 4}
                                    ,       - match literal ',' (comma)
                                     (\d)   - match any digit {group 5}
                                         $  - match end of string
     */
    public static final Pattern PARAM_CSV = compile("^p_(i??)(\\d+?)_(\\d+?)_??,(.+?),(\\d)$");

    /*
      ^                                    - match start of string
       (?<reobf>[$\w/]+)                   - match 1 or more of: [$ (dollar sign), any word character,
                                                                  / (forward slash)] {named group: reobf}
                       " "                 - match literal ' ' (space)
                         (?<srg>[$\w/]+)   - match 1 or more of: [$ (dollar sign), any word character,
                                                                  / (forward slash)] {named group: srg}
                                        $  - match end of string
     */
    public static final Pattern TSRG_CLASS_HEADER = compile("^(?<reobf>[$\\w/]+) (?<srg>[$\\w/]+)$");
    /*
      ^                                                           - match start of string
       \s*                                                        - match 0 or more of any whitespace character
          (?<reobf>[$\w/]+)                                       - match 1 or more of: [$ (dollar sign), any word character,
                                                                                         / (forward slash)] {named group: reobf}
                          " "                                     - match literal ' ' (space)
                            (?<srg>                               - start {named group: srg}
                                   field_                         - match literal 'field_'
                                         (?<id>\d+)
                                                   _              - match literal '_' (underscore)
                                                    [$\w/]+?      - lazily match 1 or more of: [$ (dollar sign),
                                                                                                any word character,
                                                                                                / (forward slash)]
                                                            _?    - match 0 or 1 of literal '_' (underscore)
                                                              )   - end group
                                                               $  - match end of string
     */
    public static final Pattern TSRG_FIELD = compile("^\\s*(?<reobf>[$\\w/]+) (?<srg>field_(?<id>\\d+)_[$\\w/]+?_?)$");
    /*
      ^                                         - match start of string
       \s*                                      - match 0 or more of any whitespace character
          (?<reobf>[$\w/]+)                     - match 1 or more of: [$ (dollar sign), any word character, / (forward slash)]
                                                  {named group: reobf}
                          " "                   - match literal ' ' (space)
                            (?<value>[$\w/]+)   - match 1 or more of: [$ (dollar sign), any word character, / (forward slash)]
                                                  {named group: value}
                                             $  - match end of string
     */
    public static final Pattern TSRG_ENUM_VALUE = compile("^\\s*(?<reobf>[$\\w/]+) (?<value>[$\\w/]+)$");
    /*
      ^                                                                             - match start of string
       \s*                                                                          - match 0 or more of any whitespace
                                                                                      character
          (?<reobf>[$\w/]+?)                                                        - lazily match 1 or more of:
                                                                                      [$ (dollar sign), any word character,
                                                                                       / (forward slash)] {named group: reobf}
                           " "                                                      - match literal ' ' (space)
                             (?<signature>.+?)                                      - lazily match 1 or more of any character
                                                                                      except newline {named group: signature}
                                             " "                                    - match literal ' ' (space)
                                               (?<srg>                              - start {named group: srg}
                                                      func_                           - match literal 'func_'
                                                           (?<id>\d+)                 - match 1 or more of any digit
                                                                                        {named group: id}
                                                                     _                - match literal '_' (underscore)
                                                                      [$\w/]+?        - lazily match 1 or more of:
                                                                                        [$ (dollar sign), any word character,
                                                                                         / (forward slash)]
                                                                              _?      - match 0 or 1 of literal '_' (underscore)
                                                                                )   - end group
                                                                                 $  - match end of string
     */
    public static final Pattern TSRG_NUMBERED_METHOD =
        compile("^\\s*(?<reobf>[$\\w/]+?) (?<signature>.+?) (?<srg>func_(?<id>\\d+)_[$\\w/]+?_?)$");
    /*
      ^                                                            - match start of string
       \s*                                                         - match 0 or more of any whitespace character
          (?<reobf>[$\w/]+?)                                       - lazily match 1 or more of: [$ (dollar sign),
                                                                                                 any word character,
                                                                                                 / (forward slash)]
                                                                     {named group: reobf}
                           " "                                     - match literal ' ' (space)
                             (?<signature>.*?)                     - lazily match 0 or more of any character except newline
                                                                     {named group: signature}
                                             " "                   - match literal ' ' (space)
                                               (?<deobf>[$\w/]+)   - match 1 or more of: [$ (dollar sign), any word character,
                                                                                         / (forward slash)]
                                                                     {named group: deobf}
                                                                $  - match end of string
     */
    public static final Pattern TSRG_NAMED_METHOD = compile("^\\s*(?<reobf>[$\\w/]+?) (?<signature>.*?) (?<deobf>[$\\w/]+)$");

    /*
      L                - match literal 'L'
       (?<class>.+?)   - lazily match 1 or more of any character except newline {named group: class}
                    ;  - match literal ';' (semicolon)
     */
    public static final Pattern METHOD_DESCRIPTOR__REFERENCE_TYPE = compile("L(?<class>.+?);");

    /*
      \(                       - match literal '(' (opening parenthesis)
        (?<parameters>.*)      - match 0 or more of any character except newline {named group: parameter}
                         \)    - match literal ')' (closing parenthesis)
                           .+  - match 1 or more of any character except newline
     */
    public static final Pattern METHOD_SIGNATURE_PARAMETERS = compile("\\((?<parameters>.*)\\).+");
    /*
      (?<descriptor>                      - start {named group: descriptor}
                    (?:                     - start {non-capturing group}
                       L                      - match literal 'L'
                        .*?                   - lazily match 0 or more of any character except newline
                           ;                  - match literal ';' (semicolon)
                            )               - end group
                             |              - | match EITHER the previous token OR the next token
                              [SBIJZDF]     - match any of: [S, B, I, J, Z, D, F]
                                       )  - end group
     */
    public static final Pattern METHOD_DESCRIPTOR = compile("(?<descriptor>(?:L.*?;)|[SBIJZDF])");

    /*
      (?<type>func|field)                           - match EITHER literal 'func' OR 'field' {named group: type}
                         _                          - match literal '_' (underscore)
                          (?<id>\d+)                - match 1 or more of any digit {named group: id}
                                    _               - match literal '_' (underscore)
                                     (?:[$\w/]*)    - match 0 or more of: [$ (dollar sign), any word character,
                                                                           / (forward slash)]
                                                _?  - match 0 or 1 of literal '_' (underscore)
     */
    public static final Pattern SRG_NUMBER = compile("(?<type>func|field)_(?<id>\\d+)_(?:[$\\w/]*)_?");
    /*
      ^                                                          - match start of string
       (?<id>\d+)                                                - match 1 or more of any digit {named group: id}
                " "                                              - match literal ' ' (space)
                  (?<class>[$\w/]+)                              - match 1 or more of: [$ (dollar sign), any word character,
                                                                                        / (forward slash)] {named group: class}
                                  " "                            - match literal ' ' (space)
                                    (?<signature>                - start {named group: signature}
                                                 [\[();$/\w]+      - match 1 or more of; ['[' (opening bracket),
                                                                                          '(' (opening parenthesis),
                                                                                          ')' (closing parenthesis),
                                                                                          ; (semicolon),
                                                                                          $ (dollar sign),
                                                                                          / (forward slash)
                                                                                          any word character]
                                                             )   - end group
                                                              $  - match end of string
     */
    public static final Pattern CONSTRUCTOR_ENTRY = compile("^(?<id>\\d+) (?<class>[$\\w/]+) (?<signature>[\\[();$/\\w]+)$");

    private Patterns() {} // Prevent instantiation
}
