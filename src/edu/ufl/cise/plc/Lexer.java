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
            return new Token(IToken.Kind.EOF, "", currPosition, lineNumber, colNumber);
        }
        int currTokenLine = lineNumber; int currTokenCol = colNumber;
        
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
                        case '\t', ' ' -> {
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

                        //More than one state

                        case '0' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_ZERO;
                        }
                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','Y','Z',
                        '_','$' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.IN_IDENT;
                        }
                        case '1','2','3','4','5','6','7','8','9' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.IN_NUM;
                        }
                        case '"' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.IN_STRING;
                        }
                        case '-' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_MINUS;
                        }
                        case '!' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_EX;
                        }
                        case '=' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_EQ;
                        }
                        case '<' -> {
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_LT; //less than
                        }
                        case '>' -> { //greater than
                            currTokenCol = colNumber;
                            currTokenLine = lineNumber;
                            state = State.HAVE_GT;
                        }
                        case '#' -> { //comment
                            state = State.IN_COMMENT;
                        }
                        default -> {
                            state = State.ERROR;
                            throw new LexicalException("Invalid character", new IToken.SourceLocation(lineNumber, colNumber));
                        }
                    }
                }
                case HAVE_ZERO -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    if(ch == '.') {
                        state = State.HAVE_DOT;
                        //currPosition++;colNumber++;
                    }
                    else {
                        return new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                    }
                }
                case IN_NUM -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_NUM;
                            //currPosition++;colNumber++;
                        }
                        case '.' -> {
                            state = State.HAVE_DOT;
                            //currPosition++;colNumber++;
                        }
                        default -> {
                            try {
                                String tmp = input.substring(startPos, currPosition);
                                Integer.parseInt(tmp);
                                return new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            }
                            catch(Exception e) {
                                throw new LexicalException("Integer overflow", new IToken.SourceLocation(currTokenLine, currTokenCol));
                            }
                        }
                    }
                }
                case IN_IDENT -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    String tmp = input.substring(startPos, currPosition + 1);
                    if(typeSet.contains(input.substring(startPos, currPosition + 1))) {
                        currPosition++;colNumber++;
                        if (tmp.equals("void")){
                            return new Token(IToken.Kind.KW_VOID, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                        else {
                            return new Token(IToken.Kind.TYPE, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                    else if (imageOpSet.contains(input.substring(startPos, currPosition + 1))) {
                        currPosition++;colNumber++;
                        return new Token(IToken.Kind.IMAGE_OP, tmp, currPosition - 1, currTokenLine, currTokenCol);
                    }
                    else if (colorOpSet.contains(input.substring(startPos, currPosition + 1))) {
                        currPosition++;colNumber++;
                        return new Token(IToken.Kind.COLOR_OP, tmp, currPosition - 1, currTokenLine, currTokenCol);
                    }
                    else if (colorConstSet.contains(input.substring(startPos, currPosition + 1))) {
                        currPosition++;colNumber++;
                        return new Token(IToken.Kind.COLOR_CONST, tmp, currPosition - 1, currTokenLine, currTokenCol);
                    }
                    else if (boolean_LitSet.contains(input.substring(startPos, currPosition + 1))) {
                        currPosition++;colNumber++;
                        return new Token(IToken.Kind.BOOLEAN_LIT, tmp, currPosition - 1, currTokenLine, currTokenCol);
                    }
                    else if (otherKeywordsSet.contains(input.substring(startPos, currPosition + 1))) {
                        currPosition++;colNumber++;
                        if (tmp.equals("if")){
                            return new Token(IToken.Kind.KW_IF, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                        else if(tmp.equals("else")) {
                            return new Token(IToken.Kind.KW_ELSE, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                        else if(tmp.equals("fi")) {
                            return new Token(IToken.Kind.KW_FI, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                        else if(tmp.equals("write")) {
                            return new Token(IToken.Kind.KW_WRITE, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                        else if(tmp.equals("console")) {
                            return new Token(IToken.Kind.KW_CONSOLE, tmp, currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                    switch(ch) {
                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','Y','Z',
                                '_','$','0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_IDENT;
                            //currPosition++;colNumber++;
                        }
                        default -> {
                            return new Token(IToken.Kind.IDENT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                }
                case HAVE_DOT -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            state = State.IN_FLOAT;
                            //currPosition++;colNumber++;
                        }
                        default -> {
                            state = State.ERROR;
                            throw new LexicalException("Invalid dot", new IToken.SourceLocation(currTokenLine, currTokenCol));
                        }
                    }
                }
                case IN_FLOAT -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_FLOAT;
                            //currPosition++;colNumber++;
                        }
                        default -> {
                            try {
                                String tmp = input.substring(startPos, currPosition);
                                Float.parseFloat(tmp);
                                return new Token(IToken.Kind.FLOAT_LIT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            }
                            catch(Exception e) {
                                throw new LexicalException("Float overflow", new IToken.SourceLocation(currTokenLine, currTokenCol));
                            }
                        }
                    }
                }
                case IN_STRING -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '\\' -> {
                            state = State.IN_SLASH;
                            //currPosition++;colNumber++;
                        }
                        case '"' -> {
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.STRING_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
                            return token;
                        }
                        case '\n', '\r' -> {
                            state = State.IN_STRING;
                            lineNumber++;
                            colNumber = 0;
                        }
                        case '\t' -> {
                            state = State.IN_STRING;
                            colNumber++;
                        }
                        default -> {
                            state = State.IN_STRING;
                            //currPosition++;colNumber++;
                        }
                    }
                }
                case IN_SLASH -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case 'b','f','"','\'','\\','n','r','t' -> {
                            state = State.IN_STRING;
                            //currPosition++;colNumber++;
                        }
                        default -> {
                            state = State.ERROR;
                            currPosition++;colNumber++;
                            throw new LexicalException("Not a valid escape sequence", new IToken.SourceLocation(currTokenLine, currTokenCol));
                        }
                    }
                }
                case HAVE_MINUS -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '>' -> {
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.RARROW, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        default -> {
                            return new Token(IToken.Kind.MINUS, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                }
                case HAVE_EX -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '=' -> {
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.NOT_EQUALS, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        default -> {
                            return new Token(IToken.Kind.BANG, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                }
                case HAVE_LT -> { // <
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '<' -> { //<<
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.LANGLE, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        case '-' -> { //<-
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.LARROW, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        case '=' -> { //<=
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.LE, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        default -> { // <
                            return new Token(IToken.Kind.LT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                }
                case HAVE_GT -> { // >
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '>' -> { // >>
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.RANGLE, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        case '=' -> { //>=
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.GE, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        default -> {
                            return new Token(IToken.Kind.GT, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                }
                case HAVE_EQ -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '=' -> { //==
                            currPosition++;colNumber++;
                            IToken token = new Token(IToken.Kind.EQUALS, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                            return token;
                        }
                        default -> {
                            return new Token(IToken.Kind.ASSIGN, input.substring(startPos, currPosition), currPosition - 1, currTokenLine, currTokenCol);
                        }
                    }
                }
                case IN_COMMENT -> {
                    currPosition++;colNumber++;
                    if(currPosition >= input.length()) {
                        break;
                    }
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '\n' -> {
                            currPosition++;
                            lineNumber++;
                            colNumber = 0;
                            state = State.START;
                        }
                        case '\r' -> {
                            currPosition++;
                            colNumber++;
                            if(input.charAt(currPosition) == '\n') {
                                currPosition++;
                                lineNumber++;
                                colNumber = 0;
                                state = State.START;
                            }
                            else {
                                throw new LexicalException("\r not followed by \n", new IToken.SourceLocation(lineNumber, colNumber));
                            }
                        }
                        default -> {
                            state = State.IN_COMMENT;
                        }
                    }
                }
            }
        }

        if(state == State.START || state == State.IN_COMMENT) {
            return new Token(IToken.Kind.EOF, "", currPosition, lineNumber, colNumber);
        }
        else if(state == State.IN_STRING) {
            throw new LexicalException("Never closed \"", new IToken.SourceLocation(currTokenLine, currTokenCol));
        }
        else if(state == State.IN_NUM) {
            return new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.IN_FLOAT) {
            return new Token(IToken.Kind.FLOAT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.IN_IDENT) {
            return new Token(IToken.Kind.IDENT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.HAVE_ZERO) {
            return new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.HAVE_LT) {
            return new Token(IToken.Kind.LT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.HAVE_GT) {
            return new Token(IToken.Kind.GT, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.HAVE_MINUS) {
            return new Token(IToken.Kind.MINUS, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.HAVE_DOT) {
            throw new LexicalException("Invalid dot", new IToken.SourceLocation(currTokenLine, currTokenCol));
        }
        else if(state == State.IN_SLASH) {
            throw new LexicalException("Not a valid escape sequence", new IToken.SourceLocation(currTokenLine, currTokenCol));
        }
        else if(state == State.HAVE_EQ) {
            return new Token(IToken.Kind.ASSIGN, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        else if(state == State.HAVE_EX) {
            return new Token(IToken.Kind.BANG, input.substring(startPos, currPosition), currPosition, currTokenLine, currTokenCol);
        }
        //Add more if statements for rest of cases
        throw new LexicalException("Invalid token");
    }

    @Override
    public IToken peek() throws LexicalException {
        int tmpPos = currPosition; int tmpLine = lineNumber; int tmpCol = colNumber;
        IToken token = next();
        currPosition = tmpPos; colNumber = tmpCol; lineNumber = tmpLine;
        return token;
    }

    public Lexer(String input) {
        this.input = input;
    }

    private final Set<String> typeSet = new HashSet<>(Arrays.asList("string","int","float","boolean","color","image","void"));
    private final Set<String> imageOpSet = new HashSet<>(Arrays.asList("getWidth", "getHeight"));
    private final Set<String> colorOpSet = new HashSet<>(Arrays.asList("getRed","getGreen","getBlue"));
    private final Set<String> colorConstSet = new HashSet<>(Arrays.asList("BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY",
            "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK","RED", "WHITE", "YELLOW"));
    private final Set<String> boolean_LitSet = new HashSet<>(Arrays.asList("true", "false"));
    private final Set<String> otherKeywordsSet = new HashSet<>(Arrays.asList("if","else","fi","write","console"));
    private final String input;
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
        IN_COMMENT,
        ERROR
    }
}
