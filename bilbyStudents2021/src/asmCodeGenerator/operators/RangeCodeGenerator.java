package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;


import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Keyword;
import parseTree.ParseNode;
import parseTree.nodeTypes.IntegerConstantNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class RangeCodeGenerator implements SimpleCodeGenerator {



	public RangeCodeGenerator() {
		super();
	}

	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("range");

		String startLabel = labeller.newLabel("start");
		String exitLoop  = labeller.newLabel("exit");
		String lenStorage= labeller.newLabel("storage-for-arrayLength");
		String counter= labeller.newLabel("counter");
		String startLoop= labeller.newLabel("loop");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);		

		/*
		 * Code in 
		 * 
		 */
		
		//System.out.println("we here");
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}
		//code.add(PStack);
		
		
		
		
		return code;
	}

}
