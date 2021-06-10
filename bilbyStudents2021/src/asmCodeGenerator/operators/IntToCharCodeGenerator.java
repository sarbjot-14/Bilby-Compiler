package asmCodeGenerator.operators;


import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;

import java.util.List;


import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class IntToCharCodeGenerator implements SimpleCodeGenerator {
	
	private ASMOpcode bTAndOpcode; //   BTAnd

	
	public IntToCharCodeGenerator(ASMOpcode bTAndOpcode) {
		super();

		this.bTAndOpcode = bTAndOpcode;

		
	}

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	


		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}
		
		// mask bottom 7 bits
		code.add(PushI, 127);
		code.add(bTAndOpcode);
		
		// shift 25 to left
		
		
	
	
		return code;
	}

}
