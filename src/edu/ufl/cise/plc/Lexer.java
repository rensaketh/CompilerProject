package edu.ufl.cise.plc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Lexer implements ILexer {
    @Override
    public IToken next() throws LexicalException {
        State state = State.START;
        int startPos = currPosition;
        if(currPosition >= input.length()) {
            IToken token = new Token(IToken.Kind.EOF, "", currPosition, lineNumber, colNumber);
            return token;
        }
        int currTokenLine = lineNumber; int currTokenCol = colNumber;
        //TODO: Need to preserve line and col number for each token, while also having general location throughout input
        while(currPosition < input.length()) { //loop through string
            switch(state) {
                case START -> {
                    startPos = currPosition;
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '\n', '\r' -> {
                            currPosition++;
                            lineNumber++;
                            colNumber = 0;
                        }
                        case '\t', ' ' -> { //have to include comment in future as well '
                            currPosition++;
                            colNumber++;
                        }
                        case '(' -> {
                            IToken token = new Token(IToken.Kind.LPAREN, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case ')' -> {
                            IToken token = new Token(IToken.Kind.RPAREN, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '[' -> {
                            IToken token = new Token(IToken.Kind.LSQUARE, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case ']' -> {
                            IToken token = new Token(IToken.Kind.RSQUARE, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '+' -> {
                            IToken token = new Token(IToken.Kind.PLUS, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '*' -> {
                            IToken token = new Token(IToken.Kind.TIMES, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '/' -> {
                            IToken token = new Token(IToken.Kind.DIV, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '%' -> {
                            IToken token = new Token(IToken.Kind.MOD, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '&' -> {
                            IToken token = new Token(IToken.Kind.AND, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '|' -> {
                            IToken token = new Token(IToken.Kind.OR, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }

                        case ';' -> {
                            IToken token = new Token(IToken.Kind.SEMI, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case ',' -> {
                            IToken token = new Token(IToken.Kind.COMMA, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '^' -> {
                            IToken token = new Token(IToken.Kind.RETURN, input.substring(startPos, currPosition + 1), currPosition, lineNumber, colNumber);
                            currPosition++;colNumber++;
                            return token;
                        }
                        /*case '#' -> { //comment

                        }*/

                        //More than one state

                        case '0' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_ZERO;
                            currPosition++;
                        }

                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','Y','Z',
                        '_','$' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.IN_IDENT;
                            currPosition++;
                        }

                        case '1','2','3','4','5','6','7','8','9' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.IN_NUM;
                            currPosition++;
                        }

                        case '"' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.IN_STRING;
                            currPosition++;
                        }
                        case '-' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_MINUS;
                            currPosition++;
                        }
                        case '!' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_EX;
                            currPosition++;
                        }
                        case '=' -> {
                            currPosition++;
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_EQ;
                        }
                        case '<' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_LT; //less than
                            currPosition++;
                        }
                        case '>' -> { //greater than
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_GT;
                            currPosition++;
                        }
                        default -> {
                            state = State.ERROR;
                            throw new LexicalException("Invalid character", new IToken.SourceLocation(lineNumber, colNumber));
                        }
                    }
                }
                case HAVE_ZERO -> {
                    char ch = input.charAt(currPosition);
                    if(ch == '.') {
                        state = State.HAVE_DOT;
                        currPosition++;colNumber++;
                    }
                    else {
                        IToken token = new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                        state = State.START;
                        return token;
                    }
                }
                case IN_NUM -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_NUM;
                            currPosition++;colNumber++;
                        }
                        case '.' -> {
                            state = State.HAVE_DOT;
                            currPosition++;colNumber++;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case IN_IDENT -> {
                    char ch = input.charAt(currPosition);
                    String tmp = input.substring(startPos, currPosition + 1);
                    if(typeSet.contains(input.substring(startPos, currPosition + 1))) {
                        if (tmp.equals("void")){
                            IToken token = new Token(IToken.Kind.KW_VOID, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                        else {
                            IToken token = new Token(IToken.Kind.TYPE, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                    else if (imageOpSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.IMAGE_OP, tmp, currPosition, currTokenLine, currTokenCol);
                        state = State.START;
                        return token;
                    }
                    else if (colorOpSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.COLOR_OP, tmp, currPosition, currTokenLine, currTokenCol);
                        state = State.START;
                        return token;
                    }
                    else if (colorConstSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.COLOR_CONST, tmp, currPosition, currTokenLine, currTokenCol);
                        state = State.START;
                        return token;
                    }
                    else if (boolean_LitSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.BOOLEAN_LIT, tmp, currPosition, currTokenLine, currTokenCol);
                        state = State.START;
                        return token;
                    }
                    else if (otherKeywordsSet.contains(input.substring(startPos, currPosition + 1))) {
                        if (tmp.equals("if")){
                            IToken token = new Token(IToken.Kind.KW_IF, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("else")) {
                            IToken token = new Token(IToken.Kind.KW_ELSE, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("fi")) {
                            IToken token = new Token(IToken.Kind.KW_FI, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("write")) {
                            IToken token = new Token(IToken.Kind.KW_WRITE, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("console")) {
                            IToken token = new Token(IToken.Kind.KW_CONSOLE, tmp, currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                    switch(ch) {
                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','Y','Z',
                                '_','$','0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_IDENT;
                            currPosition++;colNumber++;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.IDENT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case HAVE_DOT -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            state = State.IN_FLOAT;
                            currPosition++;colNumber++;
                        }
                        default -> {
                            state = State.ERROR;
                            throw new LexicalException("Invalid dot", new IToken.SourceLocation(currTokenLine, currTokenCol));
                        }
                    }
                }
                case IN_FLOAT -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_FLOAT;
                            currPosition++;colNumber++;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.FLOAT_LIT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case IN_STRING -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '\\' -> {
                            state = State.IN_SLASH;
                            currPosition++;colNumber++;
                        }
                        case '"' -> {
                            IToken token = new Token(IToken.Kind.STRING_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        default -> {
                            state = State.IN_STRING;
                            currPosition++;colNumber++;
                        }
                    }
                }
                case IN_SLASH -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case 'b','t','n','f','r','"','\'','\\' -> {
                            state = State.IN_STRING;
                            currPosition++;colNumber++;
                        }
                        default -> {
                            state = State.ERROR;
                            throw new LexicalException("Not a valid escape sequence");
                        }
                    }
                }
                case HAVE_MINUS -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '>' -> {
                            IToken token = new Token(IToken.Kind.RARROW, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.MINUS, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case HAVE_EX -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '=' -> {
                            IToken token = new Token(IToken.Kind.NOT_EQUALS, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.BANG, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case HAVE_LT -> { // <
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '<' -> { //<<
                            IToken token = new Token(IToken.Kind.LANGLE, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '-' -> { //<-
                            IToken token = new Token(IToken.Kind.LARROW, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '=' -> { //<=
                            IToken token = new Token(IToken.Kind.LE, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;

                        }
                        default -> { // <
                            IToken token = new Token(IToken.Kind.LT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case HAVE_GT -> { // >
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '>' -> { // >>
                            IToken token = new Token(IToken.Kind.RANGLE, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        case '=' -> { //>=
                            IToken token = new Token(IToken.Kind.GE, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.GT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case HAVE_EQ -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '=' -> { //==
                            IToken token = new Token(IToken.Kind.EQUALS, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            state = State.START;
                            currPosition++;colNumber++;
                            return token;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.ASSIGN, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            state = State.START;
                            return token;
                        }
                    }
                }
            }
        }

        if(state == State.IN_STRING) {
            throw new LexicalException("Never closed \"", new IToken.SourceLocation(currTokenLine, currTokenCol));
        }
        else if(state == State.IN_NUM) {
            IToken token = new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
            state = State.START;
            return token;
        }
        else if(state == State.IN_FLOAT) {
            IToken token = new Token(IToken.Kind.FLOAT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
            state = State.START;
            return token;
        }
        else if(state == State.IN_IDENT) {
            IToken token = new Token(IToken.Kind.IDENT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
            state = State.START;
            return token;
        }
        throw new LexicalException("Invalid token");
    }

    @Override
    public IToken peek() throws LexicalException {
        return null;
    }

    public Lexer(String input) {
        this.input = input;
    }

    private Set<String> typeSet = new HashSet<>(Arrays.asList("string","int","float","boolean","color","image","void"));
    private Set<String> imageOpSet = new HashSet<>(Arrays.asList("getWidth", "getHeight"));
    private Set<String> colorOpSet = new HashSet<>(Arrays.asList("getRed","getGreen","getBlue"));
    private Set<String> colorConstSet = new HashSet<>(Arrays.asList("BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY",
            "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK","RED", "WHITE", "YELLOW"));
    private Set<String> boolean_LitSet = new HashSet<>(Arrays.asList("true", "false"));
    private Set<String> otherKeywordsSet = new HashSet<>(Arrays.asList("if","else","fi","write","console"));
    private String input;
    private int currPosition = 0;
    private int lineNumber = 0; private int colNumber = 0;
    private enum State {
        START,
        IN_IDENT,
        HAVE_ZERO,
        HAVE_DOT,
        IN_FLOAT,
        IN_NUM,
        IN_STRING,
        IN_SLASH,
        HAVE_EQ,
        HAVE_MINUS,
        HAVE_EX,
        HAVE_LT,
        HAVE_GT,
        ERROR
    }
}
