package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.runtime.ConsoleIO;

public class CodeGenVisitor implements ASTVisitor {
    private String packageName;
    private StringBuilder javaProgram;
    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;
        javaProgram = new StringBuilder();
    }

    private String convertTypeToCast(Types.Type type) {
        if(type == Types.Type.INT) {
            return "(Integer) ";
        }
        else if(type == Types.Type.STRING) {
            return "(String) ";
        }
        else if(type == Types.Type.BOOLEAN) {
            return "(Boolean) ";
        }
        else if(type == Types.Type.FLOAT) {
            return "(Float) ";
        }
        return null;
    }

    private String convertTypeToString(Types.Type type) {
        if(type == Types.Type.STRING) {
            return "String";
        }
        else {
            return type.toString().toLowerCase();
        }
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(booleanLitExpr.getValue() == true) {
            sb.append("true");
        }
        else {
            sb.append("false");
        }
        return sb;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("\"" + stringLitExpr.getValue() + "\"");
        return sb;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT) {
            sb.append(convertTypeToCast(intLitExpr.getCoerceTo()));
        }
        sb.append(Integer.toString(intLitExpr.getValue()));
        return sb;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT) {
            sb.append(convertTypeToCast(floatLitExpr.getCoerceTo()));
        }
        sb.append(Float.toString(floatLitExpr.getValue()) + "f");
        return null;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Types.Type type = consoleExpr.getCoerceTo();
        sb.append(convertTypeToCast(consoleExpr.getCoerceTo()));
        sb.append("ConsoleIO.readValueFromConsole(");

        String prompt = "\"Enter ";

        if(type == Types.Type.INT) {
            sb.append("\"INT\"");
            prompt += "integer:\"";
        }
        else if(type == Types.Type.STRING) {
            sb.append("\"STRING\"");
            prompt += "string:\"";
        }
        else if(type == Types.Type.BOOLEAN) {
            sb.append("\"BOOLEAN\"");
            prompt += "boolean:\"";
        }
        else if(type == Types.Type.FLOAT) {
            sb.append("\"FLOAT\"");
            prompt += "float:\"";
        }
        sb.comma();
        sb.append(prompt);
        sb.rparen();
        return sb;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        if(unaryExpression.getOp().getKind() == IToken.Kind.BANG || unaryExpression.getOp().getKind() == IToken.Kind.MINUS) {
            CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
            sb.lparen();
            sb.append(unaryExpression.getOp().getText());
            Expr expr = unaryExpression.getExpr();
            expr.visit(this, sb);
            sb.rparen();
            return sb;
        }
        throw new UnsupportedOperationException("Currently only supporting - and !");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.lparen();
        Expr left =  binaryExpr.getLeft();
        left.visit(this, sb);
        sb.space();
        sb.append(binaryExpr.getOp().getText()).space();
        Expr right = binaryExpr.getRight();
        right.visit(this, sb);
        sb.rparen();
        return sb;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != identExpr.getType()) {
            sb.append(convertTypeToCast(identExpr.getCoerceTo()));
        }
        sb.append(identExpr.getText());
        return sb;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.lparen();

        Expr condition = conditionalExpr.getCondition();
        condition.visit(this, sb);
        sb.rparen();
        sb.append(" ? ");
        Expr trueCase = conditionalExpr.getTrueCase();
        trueCase.visit(this, sb);
        sb.append(" : ");
        Expr falseCase = conditionalExpr.getFalseCase();
        falseCase.visit(this, sb);
        return sb;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(assignmentStatement.getName() + " = ");
        Expr assign = assignmentStatement.getExpr();
        assign.visit(this, sb);
        sb.semi().newline();
        return sb;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception { //TODO:Check
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("ConsoleIO.console.println").lparen();
        writeStatement.getSource().visit(this, sb);
        sb.rparen().semi().newline();
        return sb;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(readStatement.getSource().getType() == Types.Type.CONSOLE) {
            sb.append(readStatement.getName() + " = ");
            Expr source = readStatement.getSource();
            source.visit(this, sb);
            sb.semi().newline();
            return sb;
        }
        throw new UnsupportedOperationException("Currently only reading from console");
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        sb.append("package " + packageName).semi().newline();
        //imports

        sb.append("import edu.ufl.cise.plc.runtime.*").semi().newline();
        sb.append("public class " + program.getName() + " {").newline();
        sb.append("\tpublic static " + convertTypeToString(program.getReturnType()) + " apply").lparen();
        //StringBuilder paramList = new StringBuilder();
        for(int i = 0; i < program.getParams().size(); i++) {
            program.getParams().get(i).visit(this, sb);
            //paramList.append(param);
            if(i != program.getParams().size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") {").newline();

        for(ASTNode decsStatements : program.getDecsAndStatements()) {
            sb.tab().tab();
            decsStatements.visit(this, sb);
        }
        sb.append("\t}\n}");
        return sb.delegate.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(convertTypeToString(nameDef.getType()) + " " + nameDef.getName());
        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr expr = returnStatement.getExpr();
        sb.append("return ");
        expr.visit(this, sb);
        sb.semi().newline();
        return sb;
        //return "return " + (String) returnStatement.visit(this, arg) + ";\n";
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        declaration.getNameDef().visit(this, sb);
        if(declaration.getOp() != null && (declaration.getOp().getKind() == IToken.Kind.ASSIGN || declaration.getOp().getKind() == IToken.Kind.LARROW)) {
            sb.append(" = ");
            if (declaration.getOp().getKind() == IToken.Kind.LARROW && declaration.getExpr().getType() != Types.Type.CONSOLE) {
                throw new UnsupportedOperationException("Expected console for this part of the assignment");
            }
            Expr expr = declaration.getExpr();
            expr.visit(this, sb);
        }
        sb.semi().newline();
        return sb;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        return null;
    }
}
