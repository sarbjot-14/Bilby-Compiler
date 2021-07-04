package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpPos;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpFalse;
import static asmCodeGenerator.codeStorage.ASMOpcode.Duplicate;
import static asmCodeGenerator.codeStorage.ASMOpcode.PStack;


import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class LessCodeGenerator implements SimpleCodeGenerator {
	private ASMOpcode subtractOpcode;
	private ASMOpcode jumpPosOpcode;


	public LessCodeGenerator(ASMOpcode subtractOpcode, ASMOpcode jumpPosOpcode) {
		super();
		this.subtractOpcode = subtractOpcode;
		this.jumpPosOpcode = jumpPosOpcode;
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("lessInt");

		String startLabel = labeller.newLabel("start");
		String subLabel   = labeller.newLabel("sub");
		String trueLabel  = labeller.newLabel("true");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");
		String trueZeroLabel  = labeller.newLabel("trueZero");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}

		code.add(Label, subLabel);
		code.add(subtractOpcode);
		code.add(Duplicate);
		code.add(jumpPosOpcode, falseLabel);
		code.add(JumpFalse, falseLabel);
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
