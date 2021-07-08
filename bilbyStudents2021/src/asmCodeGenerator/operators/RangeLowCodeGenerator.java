package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IntegerConstantNode;

public class RangeLowCodeGenerator implements SimpleCodeGenerator {


	public RangeLowCodeGenerator() {
		super();
	
	}

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("LowIntegerRange");

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
		
		code.add(Pop);
	

		return code;
	}

}
