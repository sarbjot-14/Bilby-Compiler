package semanticAnalyzer.types;

import java.util.ArrayList;
import java.util.Collection;

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
		else if(token.getLexeme().equals("int")){
			return PrimitiveType.INTEGER;
		}
		else if(token.getLexeme().equals("string")){
			return PrimitiveType.STRING;
		}
		else if(token.getLexeme().equals("char")){
			return PrimitiveType.CHARACTER;
		}
		else if(token.getLexeme().equals("bool")){
			return PrimitiveType.BOOLEAN;
		}
		else {
			return PrimitiveType.ERROR;
		}
	}
	
	public boolean equivalent(Type otherType) {
		return this.equals(otherType);
	}
	@Override
	public Collection<TypeVariable> getTypeVariables() {
		return new ArrayList<TypeVariable>();
	}
	@Override
	public Type concreteType() {
		return this;
	}
}
