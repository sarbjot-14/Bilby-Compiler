package tokens;

import inputHandler.Locator;

public class TypeToken extends TokenImp {
	protected TypeToken(Locator locator, String lexeme) {
		super(locator, lexeme.intern());
	}
	
	public static TypeToken make(Locator locator, String lexeme) {
		TypeToken result = new TypeToken(locator, lexeme);
		return result;
	}


	@Override
	protected String rawString() {
		return "type, " + getLexeme();
	}
}
