package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpPos;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class IntToCharCodeGenerator implements SimpleCodeGenerator {
	
	private ASMOpcode bTAndOpcode; //   BTAnd
	private ASMOpcode loadIOpcode;
	private ASMOpcode pushPCOpcode;
	private ASMOpcode subtractOpcode;
	


	public IntToCharCodeGenerator(ASMOpcode bTAndOpcode, ASMOpcode loadIOpcode, ASMOpcode pushPCOpcode, ASMOpcode subtractOpcode) {
		super();

		this.bTAndOpcode = bTAndOpcode;
		this.loadIOpcode = loadIOpcode;
		this.loadIOpcode = pushPCOpcode;
		this.loadIOpcode = subtractOpcode;
		
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	


		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		// load int just put into data
		code.add(pushPCOpcode);
		code.add(PushI, 1);
		code.add(subtractOpcode);
		code.add(loadIOpcode);
		// mask bottom 7 bits
		code.add(PushI, 127);
		code.add(bTAndOpcode);
		
		
		
		
		
		
		
		
//		code.add(Label, startLabel);
//		for(ASMCodeFragment fragment: args) {
//			code.append(fragment);
//		}
//
//		code.add(Label, subLabel);
//		code.add(subtractOpcode);
//		code.add(duplicateOpcode);
//		code.add(jumpFalseOpcode, falseLabel); 
//		code.add(Jump, trueLabel);
//
//		code.add(Label, trueLabel);
//		code.add(PushI, 1);
//		code.add(Jump, joinLabel);
//		code.add(Label, falseLabel);
//		code.add(PushI, 0);
//		code.add(Jump, joinLabel);
//		code.add(Label, joinLabel);
		return code;
	}

}
