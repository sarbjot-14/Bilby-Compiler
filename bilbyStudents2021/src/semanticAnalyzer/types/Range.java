package semanticAnalyzer.types;

import java.util.ArrayList;
import java.util.Collection;

public class Range implements Type {

	private static final int REFERENCE_TYPE_SIZE = 4;
	Type subtype;
	
	
	public Range(Type subtype) {
		
		this.subtype =subtype;
	}
	
	@Override
	public int getSize() {
		return REFERENCE_TYPE_SIZE;
	}

	@Override
	public String infoString() {
		return "<" + getSubtype() + ">";
	}

	public Type getSubtype() {
		return subtype;
	}
	
	@Override
	public String toString() {
		
		return "<" + subtype + ">"; 
	}
	
	@Override
	public boolean equivalent(Type otherType) {
		if(otherType instanceof Range) {
			Range otherArray = (Range)otherType;
			return subtype.equivalent(otherArray.getSubtype());
					
		}
		return false;
	}
	
	@Override
	public Collection<TypeVariable> getTypeVariables() {
		return subtype.getTypeVariables();
	}

	@Override
	public Type concreteType() {
		return new Range(subtype.concreteType());
	}

}
