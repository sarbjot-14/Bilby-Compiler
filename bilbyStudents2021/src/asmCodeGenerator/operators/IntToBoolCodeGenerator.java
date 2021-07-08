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

public class IntToBoolCodeGenerator implements SimpleCodeGenerator {

	private ASMOpcode jumpFalseOpcode;
	


	public IntToBoolCodeGenerator( ASMOpcode jumpFalseOpcode) {
		super();
		this.jumpFalseOpcode = jumpFalseOpcode;
		
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("intToBool");

		String startLabel = labeller.newLabel("start");
		String trueLabel  = labeller.newLabel("true");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}


		code.add(jumpFalseOpcode, falseLabel); 
		code.add(Jump, trueLabel);

		code.add(Label, trueLabel);
		code.add(PushI, 1);
		code.add(Jump, joinLabel);
		code.add(Label, falseLabel);
		code.add(PushI, 0);
		code.add(Jump, joinLabel);
		code.add(Label, joinLabel);
		return code;
	}

}
