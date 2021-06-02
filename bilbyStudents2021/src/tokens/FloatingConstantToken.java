package tokens;

import inputHandler.Locator;
import lexicalAnalyzer.LexicalAnalyzer;

public class FloatingConstantToken extends TokenImp {
	protected double value;
	
	protected FloatingConstantToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(double value) {
		this.value = value;
	}	
	public double getValue() {
		return value;
	}
	
	public static FloatingConstantToken make(Locator locator, String lexeme) {
		FloatingConstantToken result = new FloatingConstantToken(locator, lexeme);

		if(Double.parseDouble(lexeme) == Double.POSITIVE_INFINITY) {
			result.setValue(0);
			LexicalAnalyzer.lexicalError(locator, "float is too big");
		}
		else {
			result.setValue(Double.parseDouble(lexeme));
		}


		return result;
	}
	
	@Override
	protected String rawString() {
		return "floatingConstant, " + value;
	}
}
