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

public class GreaterThanEqualCodeGeneratorFloat implements SimpleCodeGenerator {
	private ASMOpcode subtractOpcode;
	private ASMOpcode jumpPosOpcode;
	private ASMOpcode JumpFZero;
	private ASMOpcode duplicateOpcode;
	private ASMOpcode convertI;


	public GreaterThanEqualCodeGeneratorFloat(ASMOpcode subtractOpcode, ASMOpcode jumpPosOpcode, ASMOpcode JumpFZero, ASMOpcode duplicateOpcode, ASMOpcode convertI) {
		super();
		this.subtractOpcode = subtractOpcode;
		this.jumpPosOpcode = jumpPosOpcode;
		this.JumpFZero = JumpFZero;
		this.duplicateOpcode = duplicateOpcode;
		this.convertI = convertI;
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("compare");

		String startLabel = labeller.newLabel("start");
		String subLabel   = labeller.newLabel("sub");
		String trueLabel  = labeller.newLabel("true");
		String trueZeroLabel  = labeller.newLabel("trueZero");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}

		code.add(Label, subLabel);
		code.add(subtractOpcode);
		code.add(duplicateOpcode);
		//code.add(PStack);
		code.add(JumpFZero, trueZeroLabel); 
		//code.add(PStack);
		//code.add(duplicateOpcode);
		code.add(jumpPosOpcode, trueLabel);
		code.add(Jump, falseLabel);

		code.add(Label, trueZeroLabel);
		code.add(Pop);
		code.add(PushI, 1);
		code.add(Jump, joinLabel);
		code.add(Label, trueLabel);
		code.add(PushI, 1);
		code.add(Jump, joinLabel);
		code.add(Label, falseLabel);
		code.add(PushI, 0);
		code.add(Jump, joinLabel);
		code.add(Label, joinLabel);
		//code.add(PStack);
		return code;
	}

}
