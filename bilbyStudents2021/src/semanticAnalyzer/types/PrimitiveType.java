package semanticAnalyzer.types;

import tokens.Token;

public enum PrimitiveType implements Type {
	BOOLEAN(1),
	INTEGER(4),
	CHARACTER(1),
	STRING(4),
	FLOAT(8),
	ERROR(0),			// use as a value when a syntax error has occurred
	NO_TYPE(0, "");		// use as a value when no type has been assigned.
	
	private int sizeInBytes;
	private String infoString;
	
	private PrimitiveType(int size) {
		this.sizeInBytes = size;
		this.infoString = toString();
	}
	private PrimitiveType(int size, String infoString) {
		this.sizeInBytes = size;
		this.infoString = infoString;
	}
	public int getSize() {
		return sizeInBytes;
	}
	public String infoString() {
		return infoString;
	}
	
	public static Type fromToken(Token token) {
		if(token.getLexeme().equals("float")){
			return PrimitiveType.FLOAT;
		}
		else {
			return PrimitiveType.ERROR;
		}
	}
}
