package tokens;

import inputHandler.LocatedChar;
import inputHandler.Locator;
import lexicalAnalyzer.LexicalAnalyzer;

public class IntegerConstantToken extends TokenImp {
	protected int value;
	
	protected IntegerConstantToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static IntegerConstantToken make(Locator locator, String lexeme) {
		IntegerConstantToken result = new IntegerConstantToken(locator, lexeme);
		try {
			result.setValue(Integer.parseInt(lexeme));
			
		}
		catch(NumberFormatException err) {
			result.setValue(0);
			LexicalAnalyzer.lexicalError(locator, "int is too big");
		}
		return result;
	}
	
	@Override
	protected String rawString() {
		return "integerConstant, " + value;
	}
}
