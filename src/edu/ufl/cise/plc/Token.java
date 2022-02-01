package edu.ufl.cise.plc;

public class Token implements IToken{
    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getText() {
        return input;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public float getFloatValue() {
        return 0;
    }

    @Override
    public boolean getBooleanValue() {
        return false;
    }

    @Override
    public String getStringValue() {
        return null;
    }

    public Token(Kind kind, String input, int position, int length) {
        this.kind = kind;
        this.input = input;
        this.pos = position;
        this.length = length;
    }

    private Kind kind;
    private String input;
    private int pos;
    private int length;
}
