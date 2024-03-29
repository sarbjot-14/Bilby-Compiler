package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.AllocCodeGenerator;
import asmCodeGenerator.operators.ArrayLengthCodeGenerator;
import asmCodeGenerator.operators.BooleanAndCodeGenerator;
import asmCodeGenerator.operators.BooleanOrCodeGenerator;
import asmCodeGenerator.operators.EqualsCodeGeneratorFloat;
import asmCodeGenerator.operators.EqualsCodeGeneratorInt;
import asmCodeGenerator.operators.FloatDivideCodeGenerator;
import asmCodeGenerator.operators.FloatLeftRangeAdditionCodeGenerator;
import asmCodeGenerator.operators.FloatRightRangeAdditionCodeGenerator;
import asmCodeGenerator.operators.GreaterCodeGenerator;
import asmCodeGenerator.operators.GreaterThanEqualCodeGeneratorFloat;
import asmCodeGenerator.operators.GreaterThanEqualCodeGeneratorInt;
import asmCodeGenerator.operators.RangeHighCodeGenerator;
import asmCodeGenerator.operators.RangeInCodeGenerator;
import asmCodeGenerator.operators.IndexingCodeGenerator;
import asmCodeGenerator.operators.IntDivideCodeGenerator;
import asmCodeGenerator.operators.IntLeftRangeAdditionCodeGenerator;
import asmCodeGenerator.operators.IntRightRangeAdditionCodeGenerator;
import asmCodeGenerator.operators.IntToBoolCodeGenerator;
import asmCodeGenerator.operators.IntToCharCodeGenerator;
import asmCodeGenerator.operators.LessCodeGenerator;
import asmCodeGenerator.operators.LessFloatCodeGenerator;
import asmCodeGenerator.operators.LessThanEqualCodeGeneratorFloat;
import asmCodeGenerator.operators.LessThanEqualCodeGeneratorInt;
import asmCodeGenerator.operators.RangeLowCodeGenerator;
import asmCodeGenerator.operators.NotCodeGenerator;
import asmCodeGenerator.operators.NotEqualsCodeGeneratorFloat;
import asmCodeGenerator.operators.NotEqualsCodeGeneratorInt;
import asmCodeGenerator.operators.RangeCastCodeGenerator;
import asmCodeGenerator.operators.RangeCodeGenerator;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.Range;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeVariable;

import static semanticAnalyzer.types.PrimitiveType.*;


public class FunctionSignatures extends ArrayList<FunctionSignature> {
	private static final long serialVersionUID = -4907792488209670697L;
	private static Map<Object, FunctionSignatures> signaturesForKey = new HashMap<Object, FunctionSignatures>();
	
	Object key;
	
	public FunctionSignatures(Object key, FunctionSignature ...functionSignatures) {
		this.key = key;
		for(FunctionSignature functionSignature: functionSignatures) {
			add(functionSignature);
		}
		signaturesForKey.put(key, this);
	}
	
	public Object getKey() {
		return key;
	}
	public boolean hasKey(Object key) {
		return this.key.equals(key);
	}
	
	public FunctionSignature acceptingSignature(List<Type> types) {
		for(FunctionSignature functionSignature: this) {
			if(functionSignature.accepts(types)) {
				return functionSignature;
			}
		}
		return FunctionSignature.nullInstance();
	}
	public boolean accepts(List<Type> types) {
		return !acceptingSignature(types).isNull();
	}

	public List<PromotedSignature> leastLevelPromotions(List<Type> actuals) {
		List<PromotedSignature> allPromotions = PromotedSignature.makeAll(this,actuals);
		// allPromotions is all the promotions we can apply to the actual signature?
		
	
		List<List<PromotedSignature>> byNumPromotions = new ArrayList<>();
		// make a list of each promotion level
		for(int i=0; i<=actuals.size();i++) {
			byNumPromotions.add(new ArrayList<PromotedSignature>());
			
		}
		// sort them into lists
		for(PromotedSignature promotedSignature :allPromotions) {
			byNumPromotions.get(promotedSignature.numPromotions()).add(promotedSignature);
		}
		
		// return first one that isnt empty, or empty 
		for(int i=0; i<actuals.size();i++) {
			if(!byNumPromotions.get(i).isEmpty()) {
				return byNumPromotions.get(i);
			}
		}
		return byNumPromotions.get(0); //empty list
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// access to FunctionSignatures by key object.
	
	public static FunctionSignatures nullSignatures = new FunctionSignatures(0, FunctionSignature.nullInstance());

	public static FunctionSignatures signaturesOf(Object key) {
		if(signaturesForKey.containsKey(key)) {
			return signaturesForKey.get(key);
		}
		return nullSignatures;
	}
	public static FunctionSignature signature(Object key, List<Type> types) {
		FunctionSignatures signatures = FunctionSignatures.signaturesOf(key);
		return signatures.acceptingSignature(types);
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////
	// Put the signatures for operators in the following static block.
	
	static {
		// here's one example to get you started with FunctionSignatures: the signatures for addition.		
		// for this to work, you should statically import PrimitiveType.*

		new FunctionSignatures(Punctuator.ADD,
		    new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER),
		    new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT),
		    new FunctionSignature(ASMOpcode.Add, INTEGER, INTEGER, INTEGER),
		    new FunctionSignature(ASMOpcode.FAdd, FLOAT, FLOAT, FLOAT),
		    new FunctionSignature(new IntLeftRangeAdditionCodeGenerator(),new Range(INTEGER),INTEGER, new Range(INTEGER)),
		    new FunctionSignature(new IntRightRangeAdditionCodeGenerator(), INTEGER,new Range(INTEGER),new Range(INTEGER)),
		    new FunctionSignature(new FloatLeftRangeAdditionCodeGenerator(),new Range(FLOAT),FLOAT,new Range(FLOAT)),
		    new FunctionSignature(new FloatRightRangeAdditionCodeGenerator(),FLOAT,new Range(FLOAT),new Range(FLOAT))
		    ///new FunctionSignature(new FloatLeftRangeAdditionCodeGenerator(),new Range(FLOAT),FLOAT,new Range(FLOAT)
		    //new FunctionSignature(new FloatRightRangeAdditionCodeGenerator(), FLOAT,new Range(FLOAT),new Range(FLOAT))
		);
		
		new FunctionSignatures(Punctuator.SUBTRACT,
				new FunctionSignature(ASMOpcode.Negate, INTEGER, INTEGER),
			    new FunctionSignature(ASMOpcode.FNegate, FLOAT, FLOAT),
			    new FunctionSignature(ASMOpcode.Subtract, INTEGER, INTEGER, INTEGER),
			    new FunctionSignature(ASMOpcode.FSubtract, FLOAT, FLOAT, FLOAT)
		);
		
		new FunctionSignatures(Punctuator.MULTIPLY,
			    new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER),
			    new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT),
			    new FunctionSignature(ASMOpcode.Multiply, INTEGER, INTEGER, INTEGER),
			    new FunctionSignature(ASMOpcode.FMultiply, FLOAT, FLOAT, FLOAT)
			);
		
		new FunctionSignatures(Punctuator.DIVIDE,
			    new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER),
			    new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT),
			    new FunctionSignature(new IntDivideCodeGenerator(ASMOpcode.FSubtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate), INTEGER, INTEGER, INTEGER),
			    new FunctionSignature(new FloatDivideCodeGenerator(ASMOpcode.FSubtract, ASMOpcode.JumpFPos, ASMOpcode.JumpFZero, ASMOpcode.Duplicate), FLOAT, FLOAT,FLOAT)
			    //new FunctionSignature(ASMOpcode.FDivide, FLOAT, FLOAT, FLOAT)
			);
		
		new FunctionSignatures(Punctuator.GREATER, 
				new FunctionSignature(new GreaterCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new GreaterCodeGenerator(ASMOpcode.FSubtract, ASMOpcode.JumpFPos), FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(new GreaterCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos), CHARACTER, CHARACTER, BOOLEAN)
		);
		
		
		new FunctionSignatures(Punctuator.GREATER_THAN_EQUAL, 
				new FunctionSignature(new GreaterThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new GreaterThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new GreaterThanEqualCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFZero, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
		);
		new FunctionSignatures(Punctuator.LESS, 
				new FunctionSignature(new LessCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new LessFloatCodeGenerator(ASMOpcode.FSubtract, ASMOpcode.JumpFPos), FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(new LessCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos),CHARACTER,CHARACTER,BOOLEAN)
		);
		new FunctionSignatures(Punctuator.LESS_THAN_EQUAL, 
				new FunctionSignature(new LessThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new LessThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new LessThanEqualCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFZero, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
		);
		new FunctionSignatures(Punctuator.EQUALS, 
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),STRING,STRING,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),BOOLEAN,BOOLEAN,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFZero, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
				
		);
		new FunctionSignatures(Punctuator.NOT_EQUALS, 
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),STRING,STRING,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),BOOLEAN,BOOLEAN,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFZero, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
		);
	
		
		new FunctionSignatures(Punctuator.AND, 
				new FunctionSignature(new BooleanAndCodeGenerator(),BOOLEAN,BOOLEAN,BOOLEAN)
		);
		
		new FunctionSignatures(Punctuator.OR, 
				new FunctionSignature(new BooleanOrCodeGenerator(),BOOLEAN,BOOLEAN,BOOLEAN)
		);

		new FunctionSignatures(Keyword.IN,
				new FunctionSignature(new RangeInCodeGenerator(ASMOpcode.Subtract,ASMOpcode.JumpPos,ASMOpcode.Duplicate,ASMOpcode.DataI,
						ASMOpcode.StoreI,ASMOpcode.LoadI,ASMOpcode.JumpNeg),INTEGER,new Range(INTEGER),BOOLEAN),
				new FunctionSignature(new RangeInCodeGenerator(ASMOpcode.FSubtract,ASMOpcode.JumpFPos,ASMOpcode.Duplicate,ASMOpcode.DataF,
						ASMOpcode.StoreF,ASMOpcode.LoadF,ASMOpcode.JumpFNeg),FLOAT,new Range(FLOAT),BOOLEAN),
				new FunctionSignature(new RangeInCodeGenerator(ASMOpcode.Subtract,ASMOpcode.JumpPos,ASMOpcode.Duplicate,ASMOpcode.DataI,
						ASMOpcode.StoreI,ASMOpcode.LoadI,ASMOpcode.JumpNeg),CHARACTER,new Range(CHARACTER),BOOLEAN)
				);
		new FunctionSignatures(Punctuator.NOT, 
				new FunctionSignature(new NotCodeGenerator(),BOOLEAN,BOOLEAN)
		);
		TypeVariable rangeT = new TypeVariable("rangeT");
		new FunctionSignatures(Keyword.LOW, 
				new FunctionSignature(new RangeLowCodeGenerator(),new Range(rangeT),rangeT)
		);
		new FunctionSignatures(Keyword.HIGH, 
				new FunctionSignature(new RangeHighCodeGenerator(),new Range(rangeT),rangeT)
		);
		TypeVariable arrayL = new TypeVariable("arrayL");
		new FunctionSignatures(Keyword.LENGTH, 
				new FunctionSignature(new ArrayLengthCodeGenerator(),new Array(arrayL),INTEGER)
		);
		
		TypeVariable S = new TypeVariable("S");
		new FunctionSignatures(Keyword.ALLOC, 
				new FunctionSignature(new AllocCodeGenerator(),new Array(S),INTEGER, new Array(S))
		);
		
		new FunctionSignatures(Punctuator.INDEXING, 
				new FunctionSignature(new IndexingCodeGenerator(),new Array(S),INTEGER, S)
		);
		
		TypeVariable C = new TypeVariable("C");
		new FunctionSignatures(Punctuator.CAST, 
				new FunctionSignature(ASMOpcode.ConvertF, INTEGER,FLOAT, FLOAT),

				new FunctionSignature(ASMOpcode.ConvertI, FLOAT,INTEGER, INTEGER),
				
				new FunctionSignature(ASMOpcode.Nop, CHARACTER,INTEGER, INTEGER),
				new FunctionSignature(new IntToCharCodeGenerator(ASMOpcode.BTAnd), INTEGER,CHARACTER, CHARACTER),
				new FunctionSignature(new IntToBoolCodeGenerator(ASMOpcode.JumpFalse), INTEGER,BOOLEAN, BOOLEAN),
				
				new FunctionSignature(new IntToBoolCodeGenerator(ASMOpcode.JumpFalse), CHARACTER,BOOLEAN, BOOLEAN),				
				new FunctionSignature(ASMOpcode.Nop, INTEGER,INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.Nop, FLOAT,FLOAT, FLOAT),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER,CHARACTER, CHARACTER),
				new FunctionSignature(ASMOpcode.Nop, BOOLEAN,BOOLEAN, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, STRING,STRING, STRING),
				new FunctionSignature(ASMOpcode.Nop, new Range(C),new Range(C),new Range(C))				
		);///new RangeCastCodeGenerator(ASMOpcode.JumpFalse)
		TypeVariable R = new TypeVariable("R");
		new FunctionSignatures(Punctuator.RANGE_DELIM, 
				new FunctionSignature(new RangeCodeGenerator(),R,R,new Range(R))
				
		);
													
		
		// First, we use the operator itself (in this case the Punctuator ADD) as the key.
		// Then, we give that key two signatures: one an (INT x INT -> INT) and the other
		// a (FLOAT x FLOAT -> FLOAT).  Each signature has a "whichVariant" parameter where
		// I'm placing the instruction (ASMOpcode) that needs to be executed.
		//
		// I'll follow the convention that if a signature has an ASMOpcode for its whichVariant,
		// then to generate code for the operation, one only needs to generate the code for
		// the operands (in order) and then add to that the Opcode.  For instance, the code for
		// floating addition should look like:
		//
		//		(generate argument 1)	: may be many instructions
		//		(generate argument 2)   : ditto
		//		FAdd					: just one instruction
		//
		// If the code that an operator should generate is more complicated than this, then
		// I will not use an ASMOpcode for the whichVariant.  In these cases I typically use
		// a small object with one method (the "Command" design pattern) that generates the
		// required code.

	}



	

}
