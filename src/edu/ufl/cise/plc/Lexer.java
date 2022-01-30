package edu.ufl.cise.plc;

public class Lexer implements ILexer {
    @Override
    public IToken next() throws LexicalException {
        State state = State.START;
        int startPos = currPosition;
        while(true) { //loop through string
            switch(state) {
                case START -> {
                    startPos = currPosition;
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '\t', '\r', ' ', '\n' -> { //have to include comment in future as well '
                            currPosition++;
                            break;
                        }
                        case '(' -> {
                            IToken token = new Token(IToken.Kind.LPAREN, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case ')' -> {
                            IToken token = new Token(IToken.Kind.RPAREN, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '[' -> {
                            IToken token = new Token(IToken.Kind.LSQUARE, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case ']' -> {
                            IToken token = new Token(IToken.Kind.RSQUARE, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '+' -> {
                            IToken token = new Token(IToken.Kind.PLUS, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '-' -> {
                            IToken token = new Token(IToken.Kind.MINUS, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '*' -> {
                            IToken token = new Token(IToken.Kind.TIMES, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '/' -> {
                            IToken token = new Token(IToken.Kind.DIV, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '%' -> {
                            IToken token = new Token(IToken.Kind.MOD, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '&' -> {
                            IToken token = new Token(IToken.Kind.AND, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '|' -> {
                            IToken token = new Token(IToken.Kind.OR, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case '!' -> {
                            IToken token = new Token(IToken.Kind.BANG, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case ';' -> {
                            IToken token = new Token(IToken.Kind.SEMI, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }
                        case ',' -> {
                            IToken token = new Token(IToken.Kind.COMMA, input.substring(startPos, currPosition + 1), currPosition, currPosition - startPos + 1);
                            currPosition++;
                            return token;
                        }







                        case '0' -> {
                            state = State.HAVE_ZERO;
                            currPosition++;
                        }

                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','Y','Z',
                        '_','$' -> {
                            state = State.IN_IDENT;
                            currPosition++;
                        }

                        case '1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_NUM;
                            currPosition++;
                        }

                        case '"' -> {
                            state = State.IN_STRING;
                            currPosition++;
                        }

                    }
                }
                case HAVE_ZERO -> {
                    char ch = input.charAt(currPosition);
                    if(ch == '.') {
                        state = State.HAVE_DOT;
                        currPosition++;
                    }
                    else {
                        IToken token = new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                        state = State.START;
                        return token;
                    }
                }
                case IN_NUM -> {
                    char ch = input.charAt(currPosition);
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_NUM;
                            currPosition++;
                        }
                        case '.' -> {
                            state = State.HAVE_DOT;
                            currPosition++;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.INT_LIT, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            return token;
                        }
                    }
                }
                case IN_IDENT -> {
                    char ch = input.charAt(currPosition);
                    if(typeSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.TYPE, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                        state = State.START;
                        return token;
                    }
                    else if (imageOpSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.IMAGE_OP, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                        state = State.START;
                        return token;
                    }
                    else if (colorOpSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.COLOR_OP, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                        state = State.START;
                        return token;
                    }
                    else if (colorConstSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.COLOR_CONST, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                        state = State.START;
                        return token;
                    }
                    else if (boolean_LitSet.contains(input.substring(startPos, currPosition + 1))) {
                        IToken token = new Token(IToken.Kind.BOOLEAN_LIT, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                        state = State.START;
                        return token;
                    }
                    else if (otherKeywordsSet.contains(input.substring(startPos, currPosition + 1))) {
                        String tmp = input.substring(startPos, currPosition + 1);
                        if (tmp.equals("if")){
                            IToken token = new Token(IToken.Kind.KW_IF, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("else")) {
                            IToken token = new Token(IToken.Kind.KW_ELSE, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("fi")) {
                            IToken token = new Token(IToken.Kind.KW_FI, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("write")) {
                            IToken token = new Token(IToken.Kind.KW_WRITE, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            return token;
                        }
                        else if(tmp.equals("console")) {
                            IToken token = new Token(IToken.Kind.KW_CONSOLE, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            return token;
                        }

                    }
                    switch(ch) {
                        case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','Y','Z',
                                '_','$','0','1','2','3','4','5','6','7','8','9' -> {
                            state = State.IN_IDENT;
                            currPosition++;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.IDENT, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
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
                            currPosition++;
                        }
                        default -> {
                            IToken token = new Token(IToken.Kind.ERROR, input.substring(startPos, currPosition), currPosition, currPosition - startPos + 1);
                            state = State.START;
                            throw new LexicalException("Invalid dot");
                        }
                    }
                }
                case IN_STRING -> {

                }
            }
        }
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

    private enum State {
        START,
        IN_IDENT,
        HAVE_ZERO,
        HAVE_DOT,
        IN_FLOAT,
        IN_NUM,
        IN_STRING,
        HAVE_EQ,
        HAVE_MINUS,
        ERROR
    }
}
