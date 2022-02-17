package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.IToken.*;

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

    public Expr expr() throws PLCException {
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

    public Expr LogicalOrExpr() throws PLCException {
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = LogicalAndExpr();
        while (isKind(OR) ) {
            IToken op = t;
            consume();
            right = LogicalAndExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr LogicalAndExpr() throws PLCException {
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

    public Expr ComparisonExpr() throws PLCException {
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

    public Expr AdditiveExpr() throws PLCException {
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

    public Expr MultiplicativeExpr() throws PLCException {
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

    public Expr UnaryExpr() throws PLCException {
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

    public Expr UnaryExprPostfix() throws PLCException {
        IToken firstToken = t;
        Expr e = PrimaryExpr();
        if(isKind(LSQUARE)) {
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
            PixelSelector p = new PixelSelector(firstPixelToken, left, right);
            return new UnaryExprPostfix(firstToken, e, p);
        }
        else {
            return e;
        }
    }

    public Expr PrimaryExpr() throws PLCException {
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

        if(isKind(LPAREN)) {
            consume();
            Expr e = expr();
            if(!isKind(RPAREN)) {
                throw new SyntaxException("Expected right parentheses");
            }
            consume();
            return e;
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
