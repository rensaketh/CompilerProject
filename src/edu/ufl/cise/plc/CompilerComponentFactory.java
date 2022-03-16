package edu.ufl.cise.plc;

//This class eliminates hard coded dependencies on the actual Lexer class.  You can call your lexer whatever you
//want as long as it implements the ILexer interface and you have provided an appropriate body for the getLexer method.


public class CompilerComponentFactory {
	
	//This method will be invoked to get an instance of your lexer.  
	public static ILexer getLexer(String input) {
		//for example:  
		return new Lexer(input);
		/*throw new UnsupportedOperationException(
				"CompilerComponentFactory must be modified to return an instance of your lexer");*/
	}

	public static IParser getParser(String input) {
		return new Parser(input);
	}

    public static Object getTypeChecker() {
		return null;
    }
}
