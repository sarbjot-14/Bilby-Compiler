package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.EqualsCodeGeneratorFloat;
import asmCodeGenerator.operators.EqualsCodeGeneratorInt;
import asmCodeGenerator.operators.GreaterCodeGenerator;
import asmCodeGenerator.operators.GreaterThanEqualCodeGeneratorFloat;
import asmCodeGenerator.operators.GreaterThanEqualCodeGeneratorInt;
import asmCodeGenerator.operators.LessCodeGenerator;
import asmCodeGenerator.operators.LessThanEqualCodeGeneratorFloat;
import asmCodeGenerator.operators.LessThanEqualCodeGeneratorInt;
import asmCodeGenerator.operators.NotEqualsCodeGeneratorFloat;
import asmCodeGenerator.operators.NotEqualsCodeGeneratorInt;
import lexicalAnalyzer.Punctuator;
import semanticAnalyzer.types.Type;
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
		    new FunctionSignature(ASMOpcode.FAdd, FLOAT, FLOAT, FLOAT)
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
			    new FunctionSignature(ASMOpcode.Divide, INTEGER, INTEGER, INTEGER),
			    new FunctionSignature(ASMOpcode.FDivide, FLOAT, FLOAT, FLOAT)
			);
		
		new FunctionSignatures(Punctuator.GREATER, 
				new FunctionSignature(new GreaterCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new GreaterCodeGenerator(ASMOpcode.FSubtract, ASMOpcode.JumpFPos), FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(new GreaterCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos), CHARACTER, CHARACTER, BOOLEAN)
		);
		
		
		new FunctionSignatures(Punctuator.GREATER_THAN_EQUAL, 
				new FunctionSignature(new GreaterThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new GreaterThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new GreaterThanEqualCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFalse, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
		);
		new FunctionSignatures(Punctuator.LESS, 
				new FunctionSignature(new LessCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new LessCodeGenerator(ASMOpcode.FSubtract, ASMOpcode.JumpFPos), FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(new LessCodeGenerator(ASMOpcode.Subtract, ASMOpcode.JumpPos),CHARACTER,CHARACTER,BOOLEAN)
		);
		new FunctionSignatures(Punctuator.LESS_THAN_EQUAL, 
				new FunctionSignature(new LessThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new LessThanEqualCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new LessThanEqualCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFalse, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
		);
		new FunctionSignatures(Punctuator.EQUALS, 
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),STRING,STRING,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),BOOLEAN,BOOLEAN,BOOLEAN),
				new FunctionSignature(new EqualsCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFalse, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
				
		);
		new FunctionSignatures(Punctuator.NOT_EQUALS, 
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),INTEGER,INTEGER,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),STRING,STRING,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),CHARACTER,CHARACTER,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorInt(ASMOpcode.Subtract, ASMOpcode.JumpPos, ASMOpcode.JumpFalse, ASMOpcode.Duplicate),BOOLEAN,BOOLEAN,BOOLEAN),
				new FunctionSignature(new NotEqualsCodeGeneratorFloat(ASMOpcode.FSubtract, ASMOpcode.JumpFPos,ASMOpcode.JumpFalse, ASMOpcode.Duplicate, ASMOpcode.ConvertI), FLOAT, FLOAT, BOOLEAN)
		);
		new FunctionSignatures(Punctuator.CAST, 
				new FunctionSignature(ASMOpcode.ConvertF, INTEGER,FLOAT, FLOAT)
				
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
