package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import semanticAnalyzer.types.Type;

public class PromotedSignature {

	private List<Promotion> promotions;
	private FunctionSignature signature;
	private Type resultType;
	
	// type variables will used in a lot of differnt matches, type variables could be reset to something
	// to something different in signature
	private PromotedSignature(FunctionSignature signature, List<Promotion> promotions) {
		//private PromotedSignature(FunctionSignature signature, List<Promotion> promotions) {
		this.signature = signature;
		this.resultType = signature.resultType().concreteType();
		this.promotions = new ArrayList<Promotion>(promotions);
	}
	
	
	public Type resultTye() {
		return resultType;
	}

	public int numPromotions() {
		int numPromotions = 0;
		 
        for(Promotion promotion: promotions) {
                if(promotion != Promotion.NONE) {
                    numPromotions++;
                }
        }
        return numPromotions;
	}

	public static List<PromotedSignature> makeAll(FunctionSignatures functionSignatures, List<Type> actuals) {
		List<PromotedSignature> all = new ArrayList<PromotedSignature>();
		// goes through all function signatures in function signatures, then add them all into a list
		for(FunctionSignature functionSignature:functionSignatures) {
			all.addAll(makeAll(functionSignature,actuals));
		}
		return all;
	}


	private static List<PromotedSignature> makeAll(FunctionSignature functionSignature,
			List<Type> actuals) {
		List<PromotedSignature> result  = new ArrayList<PromotedSignature>();
		
		// if size == 2 need two nested for Promotion.values  (fix)
		// one for first actual and one for second actual
		if(actuals.size() == 1) {
            Type actual = actuals.get(0);
            for(Promotion promotion: Promotion.values()) {
                if(promotion.applies(actual)) {
                    Type promotedActual = promotion.apply(actual);
                    PromotedSignature promotedSignature = tryTypes(functionSignature, promotion, promotedActual);
                    if(promotedSignature != nullInstance()) {
                        result.add(promotedSignature);
                    }
                }
            }
            return result;
		}
		else if(actuals.size() ==2) {
			
		}
		else {
			throw new RuntimeException("makeAll called with more than two actuals");
		}
		return null;
	}

	// need a try type that will take two promotions (fix)
	// also need promoted type of both arguments in array list
	private static PromotedSignature tryTypes(FunctionSignature functionSignature, Promotion promotion, Type promotedActual) {
		if(functionSignature.accepts(Arrays.asList(promotedActual))) {
			return new PromotedSignature(functionSignature, Arrays.asList(promotion));
		}
		else {
			return nullInstance();
		}
	}
	
	static private PromotedSignature nullInstance = null;
	private static PromotedSignature nullInstance() {
		if(nullInstance == null) {
			nullInstance = new PromotedSignature(FunctionSignature.nullInstance(), new ArrayList<Promotion>());
		}
		return nullInstance;
	}


	public Object getVariant() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
