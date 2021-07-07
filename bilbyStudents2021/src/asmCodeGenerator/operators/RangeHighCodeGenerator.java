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

public class RangeHighCodeGenerator implements SimpleCodeGenerator {


	public RangeHighCodeGenerator() {
		super();
	
	}

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("LowIntegerRange");

		String startLabel = labeller.newLabel("start");
	
		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);

		
		//code.add(PStack);
		

		/*
		 * Code in operstaor and that short circuits
		 * 
		 */
		if(node.getChildren().get(0).getChildren().get(0) instanceof IntegerConstantNode ) {
			IntegerConstantNode high = (IntegerConstantNode) node.getChildren().get(0).getChildren().get(1);
			//System.out.println(high.getValue());
			code.add(PushI,high.getValue());
		}
		else if(node.getChildren().get(0).getChildren().get(0) instanceof FloatingConstantNode ) {
			FloatingConstantNode high = (FloatingConstantNode) node.getChildren().get(0).getChildren().get(1);
			code.add(PushF,high.getValue());
		}
		else if(node.getChildren().get(0).getChildren().get(0) instanceof CharacterConstantNode ) {
			CharacterConstantNode  high = (CharacterConstantNode ) node.getChildren().get(0).getChildren().get(01);
			code.add(PushI,high.getValue());
		}

		return code;
	}

}
