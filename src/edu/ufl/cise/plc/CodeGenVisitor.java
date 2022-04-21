package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.runtime.ConsoleIO;

import java.awt.image.BufferedImage;

public class CodeGenVisitor implements ASTVisitor {
    private String packageName;
    private StringBuilder javaProgram;
    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;
        javaProgram = new StringBuilder();
    }

    private String convertTypeToCast(Types.Type type) {
        if(type == Types.Type.INT) {
            return "(int) ";
        }
        else if(type == Types.Type.STRING) {
            return "(String) ";
        }
        else if(type == Types.Type.BOOLEAN) {
            return "(boolean) ";
        }
        else if(type == Types.Type.FLOAT) {
            return "(float) ";
        }
        else if(type == Types.Type.COLOR) {
            return "(ColorTuple) ";
        }
        return null;
    }

    private String convertTypeToBoxed(Types.Type type) {
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
            return "(float) ";
        }
        else if(type == Types.Type.COLOR) {
            return "(ColorTuple) ";
        }
        return null;
    }

    private String convertTypeToString(Types.Type type) {
        if(type == Types.Type.STRING) {
            return "String";
        }
        else if(type == Types.Type.IMAGE) {
            return "BufferedImage";
        }
        else if(type == Types.Type.COLOR) {
            return "ColorTuple";
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
        sb.append("\"\"\"\n" + stringLitExpr.getValue() + "\"\"\"");
        return sb;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT && intLitExpr.getCoerceTo() != Types.Type.COLOR) {
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
        return sb;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.lparen();
        sb.append("ColorTuple.unpack(Color.");
        sb.append(colorConstExpr.getText());
        sb.append(".getRGB())").rparen();
        return sb;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Types.Type type = consoleExpr.getCoerceTo();
        sb.append(convertTypeToBoxed(consoleExpr.getCoerceTo()));
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
        else if(type == Types.Type.COLOR) {
            sb.append("\"COLOR\"");
            prompt += "red, green, and blue components separated with space:\"";
        }
        sb.comma();
        sb.append(prompt);
        sb.rparen();
        return sb;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        boolean castToFloat = false;
        if(colorExpr.getType() == Types.Type.COLORFLOAT) {
            sb.append("new ColorTupleFloat(");
        }
        else {
            sb.append("new ColorTuple(");
        }
        /*if(colorExpr.getRed().getType() == Types.Type.FLOAT) {
            sb.append("(int) ");
        }*/
        colorExpr.getRed().visit(this, arg);
        sb.comma().space();
        /*if(colorExpr.getGreen().getType() == Types.Type.FLOAT) {
            sb.append("(int) ");
        }*/
        colorExpr.getGreen().visit(this, arg);
        sb.comma().space();
        /*if(colorExpr.getBlue().getType() == Types.Type.FLOAT) {
            sb.append("(int) ");
        }*/
        colorExpr.getBlue().visit(this, arg);
        sb.rparen();
        if(colorExpr.getType() == Types.Type.COLORFLOAT) {
            sb.append(".pack()");
        }
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(unaryExpression.getOp().getKind() == IToken.Kind.BANG || unaryExpression.getOp().getKind() == IToken.Kind.MINUS) {
            sb.lparen();
            sb.append(unaryExpression.getOp().getText());
            Expr expr = unaryExpression.getExpr();
            if(unaryExpression.getCoerceTo() != null && unaryExpression.getCoerceTo() != unaryExpression.getType()) {
                sb.append(convertTypeToCast(unaryExpression.getCoerceTo()));
            }
            expr.visit(this, sb);
            sb.rparen();
        }
        else if(unaryExpression.getExpr().getType() == Types.Type.COLOR || unaryExpression.getExpr().getType() == Types.Type.INT) {
            sb.append("ColorTuple.");
            sb.append(unaryExpression.getOp().getText()).lparen();
            unaryExpression.getExpr().visit(this, arg);
            sb.rparen();
            /*if(unaryExpression.getExpr().getType() == Types.Type.INT || unaryExpression.getExpr().getCoerceTo() == Types.Type.INT) {

            }*/
        }
        else if(unaryExpression.getExpr().getType() == Types.Type.IMAGE) {
            sb.append("ImageOps.extract");
            switch(unaryExpression.getOp().getText()) {
                case "getRed":
                    sb.append("Red");
                    break;
                case "getBlue":
                    sb.append("Blue");
                    break;
                case "getGreen":
                    sb.append("Green");
                    break;
            }
            sb.lparen();
            unaryExpression.getExpr().visit(this, arg);
            sb.rparen();
        }
        return sb;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.lparen();
        Expr left = binaryExpr.getLeft();
        Expr right = binaryExpr.getRight();
        Types.Type leftType = (left.getCoerceTo() == null) ? left.getType() : left.getCoerceTo();
        Types.Type rightType = (right.getCoerceTo() == null) ? right.getType() : right.getCoerceTo();
        if((leftType == Types.Type.COLOR && rightType == Types.Type.COLOR) || (leftType == Types.Type.IMAGE && rightType == Types.Type.IMAGE)) {
            if(leftType == Types.Type.COLOR && rightType == Types.Type.COLOR) {
                sb.append("ImageOps.binaryTupleOp(ImageOps.");
            }
            else {
                sb.append("ImageOps.binaryImageImageOp(ImageOps.");
            }
            if(binaryExpr.getOp().getKind() == IToken.Kind.EQUALS || binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) {
                sb.append("BoolOP.");
            }
            else {
                sb.append("OP.");
            }
            sb.append(binaryExpr.getOp().getKind().name()).comma().space();
            left.visit(this, sb);
            sb.comma().space();
            right.visit(this, sb);
            sb.rparen();
        }
        else if((leftType == Types.Type.IMAGE && (rightType == Types.Type.COLOR || rightType == Types.Type.INT)) || ((leftType == Types.Type.COLOR || leftType == Types.Type.INT) && rightType == Types.Type.IMAGE)) {
            sb.append("ImageOps.binaryImageScalarOp(ImageOps.");
            if(binaryExpr.getOp().getKind() == IToken.Kind.EQUALS || binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) {
                sb.append("BoolOP.");
            }
            else {
                sb.append("OP.");
            }
            Expr image = (leftType == Types.Type.IMAGE) ? left : right;
            Expr colorOrInt = ((leftType == Types.Type.COLOR || leftType == Types.Type.INT)) ? left : right;
            sb.append(binaryExpr.getOp().getKind().name()).comma().space();
            image.visit(this, sb);
            sb.comma().space();
            if(colorOrInt.getType() == Types.Type.INT) {
                colorOrInt.visit(this, arg);
            }
            else {
                sb.append("ColorTuple.makePackedColor(");
            }
            sb.rparen();
        }
        else {
            if (leftType == Types.Type.STRING && (binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) && rightType == Types.Type.STRING) {
                sb.append("!");
            }
            if (binaryExpr.getCoerceTo() != null && binaryExpr.getCoerceTo() != binaryExpr.getType()) {
                sb.append(convertTypeToCast(binaryExpr.getCoerceTo()));
            }
            left.visit(this, sb);
            if (leftType == Types.Type.STRING && (binaryExpr.getOp().getKind() == IToken.Kind.EQUALS || binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) && rightType == Types.Type.STRING) {
                sb.append(".equals(");
            } else {
                sb.space().append(binaryExpr.getOp().getText()).space();
            }

            if (binaryExpr.getCoerceTo() != null && binaryExpr.getCoerceTo() != binaryExpr.getType()) {
                sb.append(convertTypeToCast(binaryExpr.getCoerceTo()));
            }
            right.visit(this, sb);
            if (leftType == Types.Type.STRING && (binaryExpr.getOp().getKind() == IToken.Kind.EQUALS || binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) && rightType == Types.Type.STRING) {
                sb.rparen();
            }
        }
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
        //sb.lparen();
        Expr condition = conditionalExpr.getCondition();
        condition.visit(this, sb);
        //sb.rparen();
        sb.append(" ? ");
        Expr trueCase = conditionalExpr.getTrueCase();
        if(conditionalExpr.getCoerceTo() != null && conditionalExpr.getCoerceTo() != conditionalExpr.getType()) {
            sb.append(convertTypeToCast(conditionalExpr.getCoerceTo()));
        }
        trueCase.visit(this, sb);
        sb.append(" : ");
        Expr falseCase = conditionalExpr.getFalseCase();
        if(conditionalExpr.getCoerceTo() != null && conditionalExpr.getCoerceTo() != conditionalExpr.getType()) {
            sb.append(convertTypeToCast(conditionalExpr.getCoerceTo()));
        }
        falseCase.visit(this, sb);
        sb.rparen();
        return sb;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        dimension.getWidth().visit(this, arg);
        sb.comma().space();
        dimension.getHeight().visit(this, arg);
        return sb;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        StringBuilder[] sbArr;
        pixelSelector.getX().visit(this, arg);
        pixelSelector.getY().visit(this, arg);
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(assignmentStatement.getTargetDec().getType() == Types.Type.IMAGE && assignmentStatement.getTargetDec().getDim() != null) {
            String xVar = assignmentStatement.getSelector().getX().getText();
            String yVar = assignmentStatement.getSelector().getY().getText();
            sb.append("for(int " + xVar + " = 0; " + xVar + " < ");
            sb.append(assignmentStatement.getName() + ".getWidth(); " + xVar + "++) {\n\t\t\t");
            sb.append("for(int " + yVar + " = 0; " + yVar + " < ");
            sb.append(assignmentStatement.getName() + ".getHeight(); " + yVar + "++) {\n\t\t\t\t");
            sb.append("ImageOps.setColor(");
            sb.append(assignmentStatement.getName()).comma().space();
            sb.append(xVar + ", " + yVar + ", ");
            if(assignmentStatement.getExpr().getCoerceTo() == Types.Type.COLOR) {
                Expr assign = assignmentStatement.getExpr();
                assign.visit(this, sb);
                sb.rparen().semi().newline();
                //sb.append("\t\t\t}");
                //sb.append("\n\t\t}\n");
                //new ColorTuple("
            }
            else if(assignmentStatement.getExpr().getCoerceTo() == Types.Type.INT || (assignmentStatement.getExpr().getCoerceTo() == null && assignmentStatement.getExpr().getType() == Types.Type.INT)) {
                sb.append("ColorTuple.unpack(ColorTuple.truncate(");
                Expr assign = assignmentStatement.getExpr();
                assign.visit(this, sb);
                sb.rparen().rparen().rparen().semi().newline();
            }
            sb.append("\t\t\t}");
            sb.append("\n\t\t}\n");
        }
        else {
            sb.append(assignmentStatement.getName() + " = ");
            Expr assign = assignmentStatement.getExpr();
            assign.visit(this, sb);
            sb.semi().newline();
        }
        return sb;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception { //TODO:Check
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(writeStatement.getSource().getType() == Types.Type.IMAGE) {
            if(writeStatement.getDest().getType() == Types.Type.CONSOLE) {
                sb.append("ConsoleIO.displayImageOnScreen").lparen();
                writeStatement.getSource().visit(this, sb);
            }
            else if(writeStatement.getDest().getType() == Types.Type.STRING) {
                sb.append("FileURLIO.writeImage").lparen();
                writeStatement.getSource().visit(this, sb);
                sb.comma().space();
                writeStatement.getDest().visit(this, sb);
            }
        }
        else if(writeStatement.getDest().getType() == Types.Type.STRING) {
            sb.append("FileURLIO.writeValue").lparen();
            writeStatement.getSource().visit(this, sb);
            sb.comma().space();
            writeStatement.getDest().visit(this, sb);
        }
        else {
            sb.append("ConsoleIO.console.println").lparen();
            writeStatement.getSource().visit(this, sb);
        }
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
        else if(readStatement.getTargetDec().getType() == Types.Type.IMAGE) {
            if (readStatement.getTargetDec().getDim() != null) {
                sb.append("FileURLIO.readImage").lparen();
                readStatement.getSource().visit(this, sb);
                sb.comma().space();
                readStatement.getTargetDec().getDim().visit(this, arg);
                sb.rparen();
            } else {
                sb.append("FileURLIO.readImage").lparen();
                readStatement.getSource().visit(this, sb);
                sb.rparen();
            }
            sb.semi().newline();
            sb.append("FileURLIO.closeFiles()").semi().newline();
        }
        return sb;
        //throw new UnsupportedOperationException("Currently only reading from console");
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        sb.append("package " + packageName).semi().newline();
        //imports
        sb.append("import java.awt.image.BufferedImage").semi().newline();
        sb.append("import java.awt.*").semi().newline();
        sb.append("import edu.ufl.cise.plc.runtime.*").semi().newline();
        //sb.append("import edu.ufl.cise.plc.runtime.ImageOps.*").semi().newline();
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
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(convertTypeToString(nameDefWithDim.getType()) + " " + nameDefWithDim.getName());
        return sb;
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

        if(declaration.getType() == Types.Type.IMAGE && declaration.getExpr() == null) {
            sb.append(" = ");
            sb.append("new BufferedImage").lparen();
            declaration.getNameDef().getDim().visit(this, arg);
            sb.comma().space();
            sb.append("BufferedImage.TYPE_INT_RGB");
            sb.rparen();
        }
        else if(declaration.getOp() != null && (declaration.getOp().getKind() == IToken.Kind.ASSIGN || declaration.getOp().getKind() == IToken.Kind.LARROW)) {
            sb.append(" = ");
            if(declaration.getType() == Types.Type.COLOR) {
                declaration.getExpr().visit(this, arg);
            }
            else if(declaration.getType() == Types.Type.IMAGE && declaration.getOp().getKind() == IToken.Kind.LARROW) {
                //declaration.
                if(declaration.getDim() != null) {
                    sb.append("FileURLIO.readImage").lparen();
                    declaration.getExpr().visit(this, sb);
                    sb.comma().space();
                    declaration.getNameDef().getDim().getWidth().visit(this, sb);
                    sb.comma().space();
                    declaration.getNameDef().getDim().getHeight().visit(this, sb);
                    sb.rparen();
                }
                else {
                    sb.append("FileURLIO.readImage").lparen();
                    declaration.getExpr().visit(this, sb);
                    sb.append(", null, null");
                    sb.rparen();
                }
            }

            else if(declaration.getType() == Types.Type.IMAGE && declaration.getExpr().getType() == Types.Type.INT) {
                sb.append("new BufferedImage").lparen();
                declaration.getNameDef().getDim().visit(this, arg);
                sb.comma().space();
                sb.append("BufferedImage.TYPE_INT_RGB");
                sb.rparen();
                sb.semi().newline();
                sb.tab().tab();
                sb.append(declaration.getNameDef().getName());
                sb.append(" = ");
                sb.append("ImageOps.setAllPixels(");
                sb.append(declaration.getNameDef().getName()).comma().space();
                declaration.getExpr().visit(this, arg);
                sb.rparen();
            }

            else if(declaration.getType() == Types.Type.IMAGE && (declaration.getExpr().getType() == Types.Type.COLOR || declaration.getExpr().getType() == Types.Type.COLORFLOAT)) {
                sb.append("new BufferedImage").lparen();
                declaration.getNameDef().getDim().visit(this, arg);
                sb.comma().space();
                sb.append("BufferedImage.TYPE_INT_RGB");
                sb.rparen();
                sb.semi().newline();
                sb.tab().tab();
                sb.append(declaration.getNameDef().getName());
                sb.append(" = ");
                sb.append("ImageOps.setColor(");
                sb.append(declaration.getNameDef().getName()).comma().space();
            }

            else {
                Expr expr = declaration.getExpr();
                expr.visit(this, sb);
            }
        }
        sb.semi().newline();
        return sb;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        /*sb.append("ColorTuple.unpack(");
        unaryExprPostfix.getExpr().visit(this, arg);
        sb.append(".getRGB").lparen();
        //unaryExprPostfix.getSelector().getX().visit(this, arg);
        sb.append("x, y").rparen();
        //unaryExprPostfix.g
        //image.ge
        //unaryExprPostfix.*/
        sb.append("ColorTuple.unpack(");
        sb.append(unaryExprPostfix.getText());
        sb.append(".getRGB").lparen();
        sb.append(unaryExprPostfix.getSelector().getX().getText()).comma().space();
        sb.append(unaryExprPostfix.getSelector().getY().getText()).rparen().rparen();
        return sb;
    }
}
