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
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;

public class FloatDivideCodeGenerator implements SimpleCodeGenerator {
	private ASMOpcode subtractOpcode;
	private ASMOpcode jumpPosOpcode;
	private ASMOpcode jumpFZero;
	private ASMOpcode duplicateOpcode;
	private ASMOpcode fDivideOpcode = ASMOpcode.FDivide;
	


	public FloatDivideCodeGenerator(ASMOpcode subtractOpcode, ASMOpcode jumpPosOpcode, ASMOpcode jumpFZero, ASMOpcode duplicateOpcode) {
		super();
		this.subtractOpcode = subtractOpcode;
		this.jumpPosOpcode = jumpPosOpcode;
		this.jumpFZero = jumpFZero;
		this.duplicateOpcode = duplicateOpcode;
		
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("compare");

		String startLabel = labeller.newLabel("start");
		String subLabel   = labeller.newLabel("sub");
		String trueLabel  = labeller.newLabel("true");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}

		code.add(Label, subLabel);
		//code.add(subtractOpcode);
		code.add(duplicateOpcode);
		code.add(jumpFZero, trueLabel); // [... a b b]. if b is zero throw runtime error
		code.add(Jump, falseLabel);

		code.add(Label, trueLabel);
		code.add(Jump, RunTime.FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR );
		code.add(Jump, joinLabel);
		code.add(Label, falseLabel); // continue dividing
		code.add(fDivideOpcode);
		code.add(Jump, joinLabel);
		code.add(Label, joinLabel);
		return code;
	}

}
