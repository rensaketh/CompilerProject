package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.IToken.*;
import edu.ufl.cise.plc.ast.*;

import java.util.ArrayList;

import static edu.ufl.cise.plc.IToken.Kind.*;

public class Parser implements IParser {
    @Override
    public ASTNode parse() throws PLCException {
        consume();
        return expr();
    }

    public Parser(String input) {
        this.input = input;
        lexer = CompilerComponentFactory.getLexer(input);
    }

    private Expr expr() throws PLCException {
        IToken firstToken = t;
        if (isKind(KW_IF)) {
            consume();
            if(!isKind(LPAREN)) {
                throw new SyntaxException("Expected left parentheses after if");
            }
            consume();
            Expr condition = expr();
            if(!isKind(RPAREN)) {
                throw new SyntaxException("Expected right parentheses after condition");
            }
            consume();
            Expr trueCase = expr();
            if(!isKind(KW_ELSE)) {
                throw new SyntaxException("Expected else statement");
            }
            consume();
            Expr falseCase = expr();
            if(!isKind(KW_FI)) {
                throw new SyntaxException("Expected fi statement");
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
            /*IToken firstPixelToken = t;
            consume();
            Expr left = expr();
            if(!isKind(COMMA)) {
                throw new SyntaxException("Expected comma");
            }
            consume();
            Expr right = expr();
            if(!isKind(RSQUARE)) {
                throw new SyntaxException("Expected right bracket");
            }
            consume();
            PixelSelector p = new PixelSelector(firstPixelToken, left, right);*/
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
            throw new SyntaxException("Expected comma");
        }
        consume();
        Expr right = expr();
        if(!isKind(RSQUARE)) {
            throw new SyntaxException("Expected right bracket");
        }
        consume();
        return new PixelSelector(firstPixelToken, left, right);
    }

    private Dimension dim() throws PLCException {
        IToken firstDimToken = t;
        consume();
        Expr left = expr();
        if(!isKind(COMMA)) {
            throw new SyntaxException("Expected comma");
        }
        consume();
        Expr right = expr();
        if(!isKind(RSQUARE)) {
            throw new SyntaxException("Expected right bracket");
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
                throw new SyntaxException("Expected comma");
            }
            consume();
            Expr green = expr();
            if(!isKind(COMMA)) {
                throw new SyntaxException("Expected comma");
            }
            Expr blue = expr();
            if(!isKind(RANGLE)) {
                throw new SyntaxException("Expected right angle");
            }
            consume();
            return new ColorExpr(firstToken, red, green, blue);
        }
        throw new SyntaxException("Expected expression not in syntax");
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

    private ILexer lexer;
    private IToken t;
    private String input;
    private ArrayList<IToken> listOfTokens = new ArrayList<>();
}
