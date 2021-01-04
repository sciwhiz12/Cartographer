package sciwhiz12.cartographer.mcp;

public interface MCPEntry {

    int srgID();

    String name();

    Side side();

    enum Side {
        CLIENT,
        DEDICATED_SERVER,
        BOTH;

        static Side of(String str) {
            if (str.equals("0")) { return Side.CLIENT; }
            if (str.equals("1")) { return Side.DEDICATED_SERVER; }
            if (str.equals("2")) { return Side.BOTH; }
            throw new IllegalArgumentException("Unknown dist/side string number " + str);
        }
    }

    interface Parameter extends MCPEntry {
        int methodSrgID();

        int index();

        @Override
        default int srgID() {
            return methodSrgID();
        }
    }

    record Field(int srgID, String name, Side side, String description) implements MCPEntry {}

    record Method(int srgID, String name, Side side, String description) implements MCPEntry {}

    record MethodParameter(int methodSrgID, int index, String name, Side side) implements Parameter {}

    record ConstructorParameter(int methodSrgID, int index, String name, Side side) implements Parameter {}
}
