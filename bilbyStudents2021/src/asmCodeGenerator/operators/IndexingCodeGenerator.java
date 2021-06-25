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

public class IndexingCodeGenerator implements SimpleCodeGenerator {

	



	public IndexingCodeGenerator() {
		super();

	
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("indexingArray");

		String startLabel = labeller.newLabel("start");
		String exitLoop  = labeller.newLabel("exit");
		String lenStorage= labeller.newLabel("storage-for-arrayLength");
		String counter= labeller.newLabel("counter");
		String startLoop= labeller.newLabel("loop");
		String subTypeSize= labeller.newLabel("subTypeSize");
		String trueLabel  = labeller.newLabel("true");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");
		String indexNumberLabel  = labeller.newLabel("indexNumber");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);		

		/*
		 * Code in 
		 * 
		 */
		//save index number
		code.append(args.get(1)); // [indexNumber]
		code.add(DLabel,indexNumberLabel); 
		code.add(DataI, 0);
		code.add(PushD, indexNumberLabel); // [indexNumber, &indexNumberVar]
		code.add(Exchange); // [&indexNumberVar, indexNumber]
		code.add(StoreI); //  [&subTypeSize]
		
		//ParseNode arrayIdentifier = node.getChildren().get(0);
		code.append(args.get(0)); //address of identifier
		code.add(PushI,8);
		code.add(Add); // now at &subTypeSize
				
				
		// store subtype size
		code.add(Duplicate);// [&subTypeSize, &subTypeSize]
		code.add(DLabel,subTypeSize); // [&subTypeSize, &subTypeSize]
		code.add(DataI, 0);
		code.add(PushD, subTypeSize); // [&subTypeSize, &subTypeSize, &subTypeVar]
		code.add(Exchange); // [&subTypeSize, &subTypeVar, &subTypeSize]
		code.add(LoadI);
		code.add(StoreI); //  [&subTypeSize]
		
		
		code.add(PushI, 4); // at length
		code.add(Add);
		
		code.add(Duplicate);
		code.add(LoadI);
		code.add(PushD,indexNumberLabel);
		code.add(LoadI);
		code.add(JumpNeg,trueLabel); // if negative throw runtime error
		code.add(PushD,indexNumberLabel);
		code.add(LoadI);
		code.add(Subtract);
		code.add(Duplicate);
		code.add(JumpFalse,trueLabel);
		code.add(JumpNeg,trueLabel);
		
		
		code.add(Jump,joinLabel);
		code.add(Label, trueLabel);
		code.add(Jump, RunTime.INDEXING_RUNTIME_ERROR );
		code.add(Label, joinLabel);
		
		
		
		//get index number
		code.add(PushD,indexNumberLabel);
		code.add(LoadI);
		// get subtype size
		code.add(PushD,subTypeSize);
		code.add(LoadI);
		code.add(Multiply);
		code.add(Add);
		code.add(PushI,4);
		code.add(Add);
		code.add(LoadI);
		
		

		
		return code;
	
		
	
	}

}
