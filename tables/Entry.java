package tables;

import typing.Type;

public class Entry {
    private String name;
    private int line;
    private Type type;

    public Entry(String name, int line, Type type) {
        this.name = name;
        this.line = line;
        this.type = type;
    }


    public String getName() {
        return this.name;
    }

    public int getLine() {
        return this.line;
    }

    public Type getType() {
        return this.type;
    }
}
