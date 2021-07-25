package semanticAnalyzer.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeVariable;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;

//immutable
public class FunctionSignatureType implements Type {
	private static final boolean ALL_TYPES_ACCEPT_ERROR_TYPES = true;
	private Type returnType;
	private Type[] paramTypes;
	//Object whichVariant;
	private Set<TypeVariable> typeVariables;
	
	
	///////////////////////////////////////////////////////////////
	// construction
	
	public FunctionSignatureType(Type returnType,Type ...types) {
		//assert(types.length >= 1);
		if(types.length>0) {
			storeParamTypes(types);
		}
		this.returnType = returnType;
		
		findTypeVariables();
	}
	private void findTypeVariables() {
		typeVariables = new HashSet<TypeVariable>();
		for(Type type:paramTypes) {
			typeVariables.addAll(type.getTypeVariables());
		}
		
	}
	private void storeParamTypes(Type[] types) {
		paramTypes = new Type[types.length-1];
		for(int i=0; i<types.length-1; i++) {
			paramTypes[i] = types[i];
		}
	}
	
	
	///////////////////////////////////////////////////////////////
	// accessors
	
//	public Object getVariant() {
//		return whichVariant;
//	}
	public Type returnType() {
		return returnType;
	}
	public boolean isNull() {
		return false;
	}
	
	
	///////////////////////////////////////////////////////////////
	// main query

	public boolean accepts(List<Type> types) {
		if(types.size() != paramTypes.length) {
			return false;
		}
		
		resetTypeVariables();
		
		for(int i=0; i<paramTypes.length; i++) {
			if(!assignableTo(paramTypes[i], types.get(i))) {
				return false;
			}
		}		
		return true;
	}
	private void resetTypeVariables() {
		
		for(TypeVariable t: typeVariables) {
			t.reset();// need to be all unconstrained
		}
		
	}
	private boolean assignableTo(Type variableType, Type valueType) {
		if(valueType == PrimitiveType.ERROR && ALL_TYPES_ACCEPT_ERROR_TYPES) {
			return true;
		}	
		return variableType.equivalent(valueType);
	}
	
	public int getNumArguments() {
		return paramTypes.length;
	}
	
	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String infoString() {
		//(int, int) -> float
		String info = "(";
		for(int i = 0; i < paramTypes.length ; i++) {
			info += paramTypes[i] + ",";
			
		}
		return info+")"+" "+"->"+" "+this.returnType ;
		
	}
	@Override
	public boolean equivalent(Type otherType) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Collection<TypeVariable> getTypeVariables() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Type concreteType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	// Null object pattern
//	private static FunctionSignatureType neverMatchedSignature = new FunctionSignatureType(1, PrimitiveType.ERROR) {
//		public boolean accepts(List<Type> types) {
//			return false;
//		}
//		public boolean isNull() {
//			return true;
//		}
//	};
//	public static FunctionSignatureType nullInstance() {
//		return neverMatchedSignature;
//	}
	
//	///////////////////////////////////////////////////////////////////
//	// Signatures for bilby-0 operators
//	// this section will probably disappear in bilby-1 (in favor of FunctionSignatures)
//	
//	private static FunctionSignatureType addSignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
//	private static FunctionSignatureType subtractSignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
//	private static FunctionSignatureType multiplySignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
//	private static FunctionSignatureType greaterSignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
//	private static FunctionSignatureType greaterThanEqualSignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
//	private static FunctionSignatureType lessSignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
//	private static FunctionSignatureType lessThanEqualSignature = new FunctionSignatureType(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
//	private static FunctionSignatureType booleanAndSignature = new FunctionSignatureType(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
//	private static FunctionSignatureType booleanOrSignature = new FunctionSignatureType(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
//	private static FunctionSignatureType notSignature = new FunctionSignatureType(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
//	// the switch here is ugly compared to polymorphism.  This should perhaps be a method on Lextant.
//	public static FunctionSignatureType signatureOf(Lextant lextant) {
//		assert(lextant instanceof Punctuator);	
//		Punctuator punctuator = (Punctuator)lextant;
//		
//		switch(punctuator) {
//		case ADD:		return addSignature;
//		case SUBTRACT:  return subtractSignature;
//		case MULTIPLY:	return multiplySignature;
//		case GREATER:	return greaterSignature;
//		case GREATER_THAN_EQUAL:	return greaterThanEqualSignature;
//		case LESS:	return lessSignature;
//		case LESS_THAN_EQUAL:	return lessThanEqualSignature;
//		case AND:	return booleanAndSignature;
//		case OR:	return booleanOrSignature;
//		case NOT:	return notSignature;
//
//		default:
//			return neverMatchedSignature;
//		}
//	}


}