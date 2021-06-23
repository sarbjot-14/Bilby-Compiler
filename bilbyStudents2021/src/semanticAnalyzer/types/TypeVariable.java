package semanticAnalyzer.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TypeVariable implements Type {
	private String name;
	private Type typeConstraint;
	
	public TypeVariable(String name) {
		this.name = name;
		this.typeConstraint = PrimitiveType.NO_TYPE;
		
	}
	
	public void reset() {
		this.typeConstraint = PrimitiveType.NO_TYPE;
		
	}
	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public String infoString() {
	
		return "$" + name;
	}
	
	public boolean equivalent(Type otherType) {
		if(otherType instanceof TypeVariable) {
			throw new RuntimeException("Type variable in actual types");
			
		}
		if(typeConstraint == PrimitiveType.NO_TYPE) {
			typeConstraint = otherType;
			return true;
		}
		else {
			return typeConstraint.equivalent(otherType);
		}
	}
	
	@Override
	public Collection<TypeVariable> getTypeVariables() {
		return Arrays.asList(this);
	}

	@Override
	public Type concreteType() {
		// take type variable out of it and return type constraint
		
		return typeConstraint.concreteType();
	}

}
