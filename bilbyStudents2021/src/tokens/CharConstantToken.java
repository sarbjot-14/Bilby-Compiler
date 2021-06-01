package tokens;

import inputHandler.Locator;

public class CharConstantToken extends TokenImp {
	protected int value;
	
	protected CharConstantToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static CharConstantToken make(Locator locator, String lexeme) {
		CharConstantToken result = new CharConstantToken(locator, lexeme);
		result.setValue(Integer.parseInt(lexeme));
		return result;
	}
	
	@Override
	protected String rawString() {
		return "integerConstant, " + value;
	}
}
