package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import semanticAnalyzer.types.Type;

public class PromotedSignature {

	public List<Promotion> promotions;
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
	
	
	public Type resultType() {
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
			
			Type actualFirst = actuals.get(0);
			Type actualSecond = actuals.get(1);
			Type promotedActualFirst = actuals.get(0);
			Type promotedActualSecond = actuals.get(1);
			for(Promotion promotion: Promotion.values()) {

				for(Promotion promotionTwo: Promotion.values()) {

					if(promotion.applies(actualFirst)) {
						promotedActualFirst = promotion.apply(actualFirst);
						if(promotionTwo.applies(actualSecond) ) {
//							System.out.println(actualSecond);
//							System.out.println(promotionTwo);
							promotedActualSecond = promotionTwo.apply(actualSecond);
//							System.out.println(promotedActualSecond);
//							System.out.println("next");
							
							PromotedSignature promotedSignature = tryTypes(functionSignature, promotion,promotionTwo, promotedActualFirst,promotedActualSecond);
							if(promotedSignature != nullInstance()) {
//								System.out.println(promotedSignature.promotions.get(0));
//								System.out.println(promotedSignature.promotions.get(1));
//								System.out.println("next");
								result.add(promotedSignature);
							}
							
						}
						
					}



				}
			}
			//System.out.format("result size in makeAll() is %d \n",result.size());
			return result;
			
		}
		else {
			throw new RuntimeException("makeAll called with more than two actuals");
		}
		
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
	
	private static PromotedSignature tryTypes(FunctionSignature functionSignature, Promotion promotion, Promotion promotionTwo, Type promotedActualFirst,Type promotedActualSecond) {
		
		if(functionSignature.accepts(Arrays.asList(promotedActualFirst,promotedActualSecond))) {
//			System.out.println(promotedActualSecond);
//			System.out.println(promotionTwo);
			
			return new PromotedSignature(functionSignature, Arrays.asList(promotion,promotionTwo));
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
		
		return signature.getVariant();
	}

	
}
