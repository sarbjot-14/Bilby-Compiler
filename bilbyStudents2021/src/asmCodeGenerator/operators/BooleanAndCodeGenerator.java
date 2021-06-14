package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;
import static asmCodeGenerator.codeStorage.ASMOpcode.PStack;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpFalse;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class BooleanAndCodeGenerator implements SimpleCodeGenerator {

	



	public BooleanAndCodeGenerator() {
		super();

	
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

		
		//code.add(PStack);
		

		/*
		 * Code in operstaor and that short circuits
		 * 
		 */

		code.append(args.get(0));
		code.add(JumpFalse, falseLabel);
		code.append(args.get(1));
		code.add(JumpFalse, falseLabel);

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
