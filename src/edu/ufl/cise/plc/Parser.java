package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.IToken.*;

import java.util.ArrayList;

import static edu.ufl.cise.plc.IToken.Kind.*;

public class Parser implements IParser {
    @Override
    public ASTNode parse() throws PLCException {
        return null;
    }

    public Parser(String input) {
        this.input = input;
        try {
            ILexer lexer = CompilerComponentFactory.getLexer(input);
            IToken token = null;
            while(token.getKind() != IToken.Kind.EOF) {
                token = lexer.next();
                listOfTokens.add(token);
            }
        }
        catch(LexicalException e) {
            System.out.println("Not parseable");
        }
    }

    private Expr LogicalOrExpr() throws PLCException{
        Expr expr = LogicalAndExpr();
        while (match(OR)) {
            IToken operator = previous();
            Expr right = LogicalAndExpr();
            expr = new BinaryExpr(listOfTokens.get(0), expr, operator, right);
        }
        return expr;
    }

    private Expr LogicalAndExpr() throws PLCException{
        Expr expr = ComparisonExpr();
        while (match(AND)) {
            IToken operator = previous();
            Expr right = ComparisonExpr();
            expr = new BinaryExpr(listOfTokens.get(0), expr, operator, right);
        }
        return expr;
    }

    private Expr ComparisonExpr() throws PLCException {
        Expr expr = AdditiveExpr();
        while (match(LT, GT, EQUALS, NOT_EQUALS, LE, GE)) {
            IToken operator = previous();
            Expr right = AdditiveExpr();
            expr = new BinaryExpr(listOfTokens.get(0),expr, operator, right);
        }
        return expr;
    }
    private Expr AdditiveExpr() throws PLCException {
        Expr expr = MultiplicativeExpr();
        while (match(PLUS, MINUS)) {
            IToken operator = previous();
            Expr right = MultiplicativeExpr();
            expr = new BinaryExpr(listOfTokens.get(0),expr, operator, right);
        }
        return expr;
    }
    private Expr MultiplicativeExpr() throws PLCException {
        Expr expr = UnaryExpr();
        while (match(TIMES, DIV, MOD)) {
            IToken operator = previous();
            Expr right = UnaryExpr();
            expr = new BinaryExpr(listOfTokens.get(0),expr, operator, right);
        }
        return expr;
    }
    private Expr UnaryExpr() throws PLCException {
        if (match(BANG, MINUS, COLOR_OP, IMAGE_OP)) {
            IToken operator = previous();
            Expr right = UnaryExpr();
            return new UnaryExpr(listOfTokens.get(0), operator, right);
        }
        return UnaryExprPostfix();
    }
    private Expr UnaryExprPostfix() throws PLCException {
        Expr expr = PrimaryExpr();
        /*while (match(OR)) {
            IToken operator = previous();
            Expr right = PixelSelector();
            expr = new BinaryExpr(listOfTokens.get(0),expr, operator, right);
        }*/
        return expr;
    }

    private Expr expression() throws PLCException {
        //need to fix
        return LogicalOrExpr();
    }
    private Expr PrimaryExpr() throws PLCException {
        if (match(BOOLEAN_LIT)) return new BooleanLitExpr(listOfTokens.get(0));
        if (match(STRING_LIT)) return new StringLitExpr(listOfTokens.get(0));
        if (match(INT_LIT)) return new IntLitExpr(listOfTokens.get(0));
        if (match(FLOAT_LIT)) return new FloatLitExpr(listOfTokens.get(0));
        if (match(IDENT)) return new IdentExpr(listOfTokens.get(0));

        if (match(LPAREN)) {
            Expr expr = expression();
            if(!check(RPAREN)) {
                throw new SyntaxException("Expect ')' after expression.");
            }
            consume();
            //return new Expr(listOfTokens.get(0));
        }

        throw new SyntaxException("Not valid syntax");
    }

    private Expr ConditionalExpr() {
        if (match(LPAREN)) {

        }
    }

    /*private Expr factor() {
        IToken token = consume();
        Expr e = null;
        if (token.getKind().equals(INT_LIT))
        {     e = new IntLitExpr(token);
            token = consume();
        }
        else if(token.getKind().equals(FLOAT_LIT))
        {
            e = new FloatLitExpr((firstToken));
            consume();
        }
        else if(token.getKind().equals(BOOLEAN_LIT))
        {
            e = new BooleanLitExpr(firstToken);
            consume();
        }
        else if(token.getKind().equals(STRING_LIT)) {
            e = new StringLitExpr(firstToken);
            e.consume();
        }
        else if (token.getKind().equals(LPAREN))
        {  consume();
            e = expr();
            match(RPAREN);
        }
        else error();
        return e;
    }*/

    private IToken consume() {
        if(currLocation == listOfTokens.size()) {
            return null;
        }
        int tmp = currLocation;
        currLocation++;
        return listOfTokens.get(tmp);
    }

    private boolean match(IToken.Kind... types) {
        for (IToken.Kind type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(IToken.Kind type) {
        if (isAtEnd()) return false;
        return peek().getKind() == type;
    }

    private IToken advance() {
        if (!isAtEnd()) currLocation++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getKind() == EOF;
    }

    private IToken peek() {
        return listOfTokens.get(currLocation);
    }

    private IToken previous() {
        return listOfTokens.get(currLocation - 1);
    }

    private String input;
    private ArrayList<IToken> listOfTokens;
    private int currLocation = 0;
}
