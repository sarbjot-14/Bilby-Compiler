package tokens;

import inputHandler.Locator;

public class CharacterConstantToken extends TokenImp {
	protected char value;
	
	protected CharacterConstantToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(char value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static CharacterConstantToken make(Locator locator, String lexeme) {
		CharacterConstantToken result = new CharacterConstantToken(locator, lexeme);
		result.setValue(lexeme.charAt(0));
		return result;
	}
	
	@Override
	protected String rawString() {
		return "characterConstant, " + value;
	}
}
