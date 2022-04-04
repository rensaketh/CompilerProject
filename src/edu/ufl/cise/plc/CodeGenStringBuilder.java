package edu.ufl.cise.plc;

public class CodeGenStringBuilder {
    StringBuilder delegate = new StringBuilder();
    //methods reimplemented—just call the delegates method
    public CodeGenStringBuilder append(String s){
        delegate.append(s);
        return this;
    }

    public CodeGenStringBuilder comma(){
        delegate.append(",");
        return this;
    }

    public CodeGenStringBuilder lparen(){
        delegate.append("(");
        return this;
    }

    public CodeGenStringBuilder rparen(){
        delegate.append(")");
        return this;
    }

    public CodeGenStringBuilder semi() {
        delegate.append(";");
        return this;
    }

    public CodeGenStringBuilder newline() {
        delegate.append("\n");
        return this;
    }

    public CodeGenStringBuilder tab() {
        delegate.append("\t");
        return this;
    }

    public CodeGenStringBuilder space() {
        delegate.append(" ");
        return this;
    }
}
