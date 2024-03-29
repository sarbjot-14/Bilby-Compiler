package semanticAnalyzer.signatures;

import semanticAnalyzer.types.Type;
import static semanticAnalyzer.types.PrimitiveType.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;

public enum Promotion {
	CHAR_TO_INT(CHARACTER,INTEGER, ASMOpcode.Nop),
	INT_TO_FLOAT(INTEGER,FLOAT,ASMOpcode.ConvertF),
	CHAR_TO_FLOAT(CHARACTER,FLOAT,ASMOpcode.ConvertF),
	NONE(NO_TYPE,NO_TYPE,ASMOpcode.Nop);
	
	Type actual;
	Type promoted;
	ASMOpcode opcode;
	
	
	private Promotion(Type actual,Type promoted,ASMOpcode opcode) {
		this.actual = actual;
		this.promoted= promoted;
		this.opcode = opcode;
		
	}

	public boolean applies(Type type) {
		return type == actual || this.equals(NONE);
	}
	
	public Type apply(Type actual) {

		return (this == NONE) ? actual : promoted;

	}
	
	public Type promotedType() {
		return promoted;
	}
	
	public ASMCodeFragment codeFor() {
		ASMCodeFragment result = new ASMCodeFragment(ASMCodeFragment.CodeType.GENERATES_VALUE) ;
		result.add(opcode);
		return result;
	}
}
