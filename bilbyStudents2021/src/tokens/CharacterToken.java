package tokens;

import inputHandler.Locator;

public class CharacterToken extends TokenImp {
	protected char value;
	
	protected CharacterToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(char value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static CharacterToken make(Locator locator, String lexeme, char value) {
		CharacterToken result = new CharacterToken(locator, lexeme);
		result.setValue(value);
		return result;
		
	}
	
	@Override
	protected String rawString() {
		return "characterConstant, " + value;
	}
}
