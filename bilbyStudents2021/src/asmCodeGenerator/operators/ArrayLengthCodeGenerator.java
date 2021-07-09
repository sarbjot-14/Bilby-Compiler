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

public class ArrayLengthCodeGenerator implements SimpleCodeGenerator {


	public ArrayLengthCodeGenerator() {
		super();
	
	}

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("Array-Length");

		String startLabel = labeller.newLabel("start");
	
		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);

		
	
		

		/*
		 * Code in operstaor and that short circuits
		 * 
		 */
		code.append(args.get(0));
		code.add(PushI,12);
		code.add(Add);
		code.add(LoadI);
		
		
		
		

		return code;
	}

}
