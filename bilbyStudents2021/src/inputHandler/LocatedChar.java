package inputHandler;

/** Value object for holding a character and its location in the input text.
 *  Contains delegates to select character operations.
 *
 */
public class LocatedChar implements Locator {
	Character character;
	TextLocation location;
	
	public LocatedChar(Character character, TextLocation location) {
		super();
		this.character = character;
		this.location = location;
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// getters
	
	public Character getCharacter() {
		return character;
	}
	public TextLocation getLocation() {
		return location;
	}
	public boolean isChar(char c) {
		return character == c;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////
	// toString
	
	public String toString() {
		return "(" + charString() + ", " + location + ")";
	}
	private String charString() {
		if(Character.isWhitespace(character)) {
			int i = character;
			return String.format("'\\%d'", i);
		}
		else {
			return character.toString();
		}
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// delegates
	
	public boolean isLowerCase() {
		return Character.isLowerCase(character) || (character == '_');
	}
	public boolean isDigit() {
		return Character.isDigit(character);
	}
	public boolean isComment() {
		return  character.charValue() == '%';
	}
	public boolean isWhitespace() {
		return Character.isWhitespace(character);
	}



	public boolean startsIdentifier() {
		// new: identifier → [ a..zA..Z_@ ][ a..zA..Z_@0..9 ] *
		boolean startsIdentifier = false;
		if(Character.isLowerCase(character) || Character.isUpperCase(character) ) {
			startsIdentifier = true;
		}
		if(character.charValue() == '_' || character.charValue() == '@'  ) {
			startsIdentifier = true;
		}
		
		return startsIdentifier;
	}


	public boolean isIdentifierChar() {
		// new: identifier → [ a..zA..Z_@ ][ a..zA..Z_@0..9 ] *
		//System.out.println(character);
		boolean startsIdentifier = false;
		if(Character.isLowerCase(character) || Character.isUpperCase(character) ) {
			startsIdentifier = true;
		}
		if(character.charValue() == '_' || character.charValue() == '@'  ) {
			startsIdentifier = true;
		}
		if(character.charValue() >= '0' && character.charValue() <= '9'  ) {
			startsIdentifier = true;
		}

		return startsIdentifier;
	}


	public boolean startsCompoundType() {
		//boolean startsCompoundType = false;
		if(character.charValue() == '<' ) {
			return true;
		}
		return false;
	}

}
