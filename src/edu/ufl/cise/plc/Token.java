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
        return loc;
    }

    @Override
    public int getIntValue() {
        return Integer.parseInt(input);
    }

    @Override
    public float getFloatValue() {
        return Float.parseFloat(input);
    }

    @Override
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(input);
    }

    @Override
    public String getStringValue() {
        String tmp = input.substring(1, input.length() - 1);
        return tmp.translateEscapes();
    }

    /*public Token(Kind kind, String input, int position, int length) {
        this.kind = kind;
        this.input = input;
        this.pos = position;
        this.length = length;
    }*/

    public Token(Kind kind, String input, int posInInput, int lineNum, int colNum) {
        this.kind = kind;
        this.input = input;
        this.pos = posInInput;
        this.loc = new SourceLocation(lineNum, colNum);
    }

    private Kind kind;
    private String input;
    private SourceLocation loc;
    private int pos;
    private int length;
}
