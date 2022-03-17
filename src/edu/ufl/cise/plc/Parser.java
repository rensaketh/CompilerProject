package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.IToken.*;

import java.util.ArrayList;
import java.util.List;

import static edu.ufl.cise.plc.IToken.Kind.*;

public class Parser implements IParser {
    @Override
    public ASTNode parse() throws PLCException {

        consume();
        ASTNode prog = program();
        if(!t.getKind().equals(EOF)) {
            throw new SyntaxException("Trailing tokens at the end of the program", t.getSourceLocation());
        }
        return prog;
        //return program();
        //return expr();
    }

    public Parser(String input) {
        this.input = input;
        lexer = CompilerComponentFactory.getLexer(input);
    }

    private Program program() throws PLCException {
        IToken firstToken = t;
        Types.Type returnType = null;
        if(!isKind(TYPE, KW_VOID)) {
            throw new SyntaxException("Expected a type or void", t.getSourceLocation());
        }
        returnType = Types.Type.toType(t.getText());
        consume();
        String name = null;
        if(!isKind(IDENT)) {
            throw new SyntaxException("Expected an identifier", t.getSourceLocation());
        }
        name = t.getText();
        consume();
        if(!isKind(LPAREN)) {
            throw new SyntaxException("Expected a (", t.getSourceLocation());
        }
        consume();
        List<NameDef> params = new ArrayList<>();

        if(isKind(TYPE)) {
            params.add(NameDefDec());
            while(isKind(COMMA)) {
                consume();
                params.add(NameDefDec());
            }
        }
        if(!isKind(RPAREN)) {
            throw new SyntaxException("Expected a )", t.getSourceLocation());
        }
        consume();
        List<ASTNode> decsAndStatements = new ArrayList<>();
        while(isKind(TYPE, IDENT, KW_WRITE, RETURN)) {
            if(isKind(TYPE)) { //Declaration
                decsAndStatements.add(declaration());
            }
            else { //Statement
                decsAndStatements.add(statement());
            }
            if(!isKind(SEMI)) {
                throw new SyntaxException("Expected semicolon", t.getSourceLocation());
            }
            consume();
        }
        return new Program(firstToken, returnType, name, params, decsAndStatements);
    }
    private Declaration declaration() throws PLCException {
        IToken firstToken = t;
        NameDef left = null;
        left = NameDefDec();
        IToken op = null;
        Expr e = null;
        if(isKind(ASSIGN, LARROW)) {
            op = t;
            consume();
            e = expr();
        }
        return new VarDeclaration(firstToken, left, op, e);
    }

    private NameDef NameDefDec() throws PLCException {
        IToken firstToken = t;
        if(isKind(TYPE)) {
            String type = t.getText();
            String name = null;
            consume();
            if(isKind(IDENT)) {
                name = t.getText();
                consume();
                return new NameDef(firstToken, type, name);
            }
            else if(isKind(LSQUARE)) {
                Dimension d = dim();
                if(!isKind(IDENT)) {
                    throw new SyntaxException("Expected identifier", t.getSourceLocation());
                }
                name = t.getText();
                consume();
                return new NameDefWithDim(firstToken, type, name, d);
            }
            throw new SyntaxException("Invalid token after type", t.getSourceLocation());
        }
        throw new SyntaxException("Expected a type", t.getSourceLocation());
    }

    private Expr expr() throws PLCException {
        IToken firstToken = t;
        if (isKind(KW_IF)) {
            consume();
            if(!isKind(LPAREN)) {
                throw new SyntaxException("Expected left parentheses after if", t.getSourceLocation());
            }
            consume();
            Expr condition = expr();
            if(!isKind(RPAREN)) {
                throw new SyntaxException("Expected right parentheses after condition", t.getSourceLocation());
            }
            consume();
            Expr trueCase = expr();
            if(!isKind(KW_ELSE)) {
                throw new SyntaxException("Expected else statement", t.getSourceLocation());
            }
            consume();
            Expr falseCase = expr();
            if(!isKind(KW_FI)) {
                throw new SyntaxException("Expected fi statement", t.getSourceLocation());
            }
            consume();
            return new ConditionalExpr(firstToken, condition, trueCase, falseCase);
        }
        return LogicalOrExpr();
    }

    private Expr LogicalOrExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = LogicalAndExpr();
        while (isKind(OR)) {
            IToken op = t;
            consume();
            right = LogicalAndExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr LogicalAndExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = ComparisonExpr();
        while (isKind(AND)) {
            IToken op = t;
            consume();
            right = ComparisonExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr ComparisonExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = AdditiveExpr();
        while (isKind(LT, GT, EQUALS, NOT_EQUALS, LE, GE)) {
            IToken op = t;
            consume();
            right = AdditiveExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr AdditiveExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = MultiplicativeExpr();
        while (isKind(PLUS, MINUS)) {
            IToken op = t;
            consume();
            right = MultiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr MultiplicativeExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = UnaryExpr();
        while (isKind(TIMES, DIV, MOD)) {
            IToken op = t;
            consume();
            right = UnaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr UnaryExpr() throws PLCException {
        IToken firstToken = t;
        Expr right = null;

        if (isKind(BANG, MINUS, COLOR_OP, IMAGE_OP)) {
            IToken op = t;
            consume();
            right = UnaryExpr();
            return new UnaryExpr(firstToken, op, right);
        }
        return UnaryExprPostfix();
    }

    private Expr UnaryExprPostfix() throws PLCException {
        IToken firstToken = t;
        Expr e = PrimaryExpr();
        if(isKind(LSQUARE)) {
            PixelSelector p = pixelSelect();
            return new UnaryExprPostfix(firstToken, e, p);
        }
        else {
            return e;
        }
    }

    private PixelSelector pixelSelect() throws PLCException {
        IToken firstPixelToken = t;
        consume();
        Expr left = expr();
        if(!isKind(COMMA)) {
            throw new SyntaxException("Expected comma", t.getSourceLocation());
        }
        consume();
        Expr right = expr();
        if(!isKind(RSQUARE)) {
            throw new SyntaxException("Expected right bracket", t.getSourceLocation());
        }
        consume();
        return new PixelSelector(firstPixelToken, left, right);
    }

    private Dimension dim() throws PLCException {
        IToken firstDimToken = t;
        consume();
        Expr left = expr();
        if(!isKind(COMMA)) {
            throw new SyntaxException("Expected comma", t.getSourceLocation());
        }
        consume();
        Expr right = expr();
        if(!isKind(RSQUARE)) {
            throw new SyntaxException("Expected right bracket", t.getSourceLocation());
        }
        consume();
        return new Dimension(firstDimToken, left, right);
    }

    private Expr PrimaryExpr() throws PLCException {
        IToken firstToken = t;
        if(isKind(BOOLEAN_LIT)) {
            consume();
            return new BooleanLitExpr(firstToken);
        }
        else if(isKind(STRING_LIT)) {
            consume();
            return new StringLitExpr(firstToken);
        }
        else if(isKind(INT_LIT)) {
            consume();
            return new IntLitExpr(firstToken);
        }
        else if(isKind(FLOAT_LIT)) {
            consume();
            return new FloatLitExpr(firstToken);
        }
        else if(isKind(IDENT)) {
            consume();
            return new IdentExpr(firstToken);
        }
        else if(isKind(COLOR_CONST)) {
            consume();
            return new ColorConstExpr(firstToken);
        }
        else if(isKind(KW_CONSOLE)) {
            consume();
            return new ConsoleExpr(firstToken);
        }
        if(isKind(LPAREN)) {
            consume();
            Expr e = expr();
            if(!isKind(RPAREN)) {
                throw new SyntaxException("Expected right parentheses");
            }
            consume();
            return e;
        }
        else if(isKind(LANGLE)) {
            consume();
            Expr red = expr();
            if(!isKind(COMMA)) {
                throw new SyntaxException("Expected comma", t.getSourceLocation());
            }
            consume();
            Expr green = expr();
            if(!isKind(COMMA)) {
                throw new SyntaxException("Expected comma", t.getSourceLocation());
            }
            consume();
            Expr blue = expr();
            if(!isKind(RANGLE)) {
                throw new SyntaxException("Expected right angle", t.getSourceLocation());
            }
            consume();
            return new ColorExpr(firstToken, red, green, blue);
        }
        throw new SyntaxException("Expected expression not in syntax", t.getSourceLocation());
    }

    private Statement statement() throws PLCException {
        IToken firstToken = t;
        if(isKind(IDENT)) {
            String name = t.getText();
            consume();
            PixelSelector p = null;
            if(isKind(LSQUARE)) {
                p = pixelSelect();
            }
            if(isKind(ASSIGN)) { //Assignment statement
                consume();
                Expr e = expr();
                return new AssignmentStatement(firstToken, name, p, e);
            }
            else if(isKind(LARROW)) { //Read statement
                consume();
                Expr e = expr();
                return new ReadStatement(firstToken, name, p, e);
            }
            else {
                throw new SyntaxException("Expected an = or <-", t.getSourceLocation());
            }
        }
        else if(isKind(KW_WRITE)) {
            consume();
            Expr left = expr();
            if(!isKind(RARROW)) {
                throw new SyntaxException("Expected right arrow ->", t.getSourceLocation());
            }
            consume();
            Expr right = expr();
            return new WriteStatement(firstToken, left, right);
        }
        else if(isKind(RETURN)) {
            consume();
            Expr e = expr();
            return new ReturnStatement(firstToken, e);
        }
        throw new SyntaxException("This is an invalid statement", t.getSourceLocation());
    }

    protected boolean isKind(Kind... kinds) {
        for (Kind k : kinds) {
            if (k == t.getKind())
                return true;
        }
        return false;
    }

    private void consume() throws PLCException {
        t = lexer.next();
        listOfTokens.add(t);
    }

    private final ILexer lexer;
    private IToken t;
    private final String input;
    private ArrayList<IToken> listOfTokens = new ArrayList<>();
}
