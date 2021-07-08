package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpPos;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class EqualsCodeGeneratorFloat implements SimpleCodeGenerator {
	private ASMOpcode subtractOpcode;
	private ASMOpcode jumpFPosOpcode;
	private ASMOpcode jumpFZeroOpcode; 
	private ASMOpcode duplicateOpcode;
	private ASMOpcode convertI;


	public EqualsCodeGeneratorFloat(ASMOpcode subtractOpcode, ASMOpcode jumpFPosOpcode, ASMOpcode jumpFZeroOpcode, ASMOpcode duplicateOpcode, ASMOpcode convertI) {
		super();
		this.subtractOpcode = subtractOpcode;
		this.jumpFPosOpcode = jumpFPosOpcode; // takes a string operand. Pops the top (floating) and Jumps if it is positive.
		this.jumpFZeroOpcode = jumpFZeroOpcode; // takes a string operand. Pops top value (integer) from stack, does Jump if value=0
		this.duplicateOpcode = duplicateOpcode;
		this.convertI = convertI;
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("equals-float");

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
		code.add(subtractOpcode);
		code.add(jumpFZeroOpcode, trueLabel); 
		
		code.add(Jump, falseLabel);

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
