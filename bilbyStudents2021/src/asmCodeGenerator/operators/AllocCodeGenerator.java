package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;


import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.runtime.MemoryManager;
import lexicalAnalyzer.Keyword;
import parseTree.ParseNode;
import parseTree.nodeTypes.IntegerConstantNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class AllocCodeGenerator implements SimpleCodeGenerator {

	



	public AllocCodeGenerator() {
		super();

	
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("alloc");

		String startLabel = labeller.newLabel("start");
		String subLabel   = labeller.newLabel("sub");
		String trueLabel  = labeller.newLabel("true");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");
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
		
		int size = node.getChildren().get(0).getChildren().get(0).getType().getSize();
		//PrimitiveType theType = (PrimitiveType) node.getChildren().get(0).getType();
		//System.out.println(node.getChildren().get(0).getChildren().get(0).getType().getSize());
		//System.out.println(size);
		
		// put asm code for expression that resolves to length of the array
		code.append(args.get(1));
		code.add(Duplicate);
		// save the len in a variable
		code.add(DLabel, lenStorage);
		code.add(DataI, 0);
		code.add(PushD,lenStorage);
		code.add(Exchange);  // ["storage-for-arrayLength", lenSize]
		code.add(StoreI);  
		
		// push size of record
		code.add(PushI, size);
		code.add(Multiply);
		code.add(PushI,16);
		code.add(Add);
			
		
		//call memory manager
		code.add(Call,MemoryManager.MEM_MANAGER_ALLOCATE);
		
		// type identifier
		code.add(Duplicate);// [&record]
		code.add(PushI, 5);
		code.add(StoreI);
		
		// status
		code.add(Duplicate);
		code.add(PushI, 4);
		code.add(Add);
		code.add(PushI, 4);
		code.add(StoreI);
		
		// Subtype size
		code.add(Duplicate);
		code.add(PushI, 8);
		code.add(Add);
		code.add(PushI, size);
		code.add(StoreI);
		
		// Length
		code.add(Duplicate);
		code.add(PushI, 12);
		code.add(Add);
		code.add(PushD,lenStorage);
		code.add(LoadI);
		code.add(StoreI);
		
		// store counter
		code.add(DLabel, counter);
		code.add(DataI,0);
		code.add(PushD,counter);
		code.add(PushI,1);
		code.add(StoreI);
		
		// start loop
		code.add(Label,startLoop);
		code.add(PushD,lenStorage);
		code.add(LoadI);
		code.add(PushI,size);
		code.add(Multiply);
		code.add(PushD,counter);
		code.add(LoadI);
		//code.add(PStack);
		code.add(Subtract);
		code.add(JumpFalse,exitLoop);
		
		
		//Elements
		code.add(Duplicate);
		code.add(PushD,counter);
		code.add(LoadI);
		//System.out.println(size);
		code.add(PushI,size);
		code.add(Multiply);
		code.add(PushI,16);
		code.add(Add);
		code.add(Add);
		
		code.add(PushI, 0);
		code.add(StoreC);
		//----------
//		if(node.getChildren().get(1).getType() == PrimitiveType.FLOAT) {
////			code.add(PushF, 0.0);
////			code.add(StoreF);
//			code.add(PushI, 0);
//			code.add(StoreC);
//			code.add(PushI, 0);
//			code.add(StoreC);
//			code.add(PushI, 0);
//		
//			
//		}
//		else if(node.getChildren().get(1).getType() == PrimitiveType.CHARACTER) { 
//			code.add(PushI, 0);
//			code.add(StoreC);
//		}
//		else if(node.getChildren().get(1).getType() == PrimitiveType.BOOLEAN) { //fix this?
//			code.add(PushI, 0);
//			code.add(StoreC);
//		}
//		else{ //integer, pointer to string, pointer to array
//			code.add(PushI, 0);
//			code.add(StoreI);
//		}
		///--------
		
		
		
		// update len
		code.add(PushD,counter);
		code.add(Duplicate); // [&lenStorage, &counter]
		code.add(LoadI); // [&lenStorage, counter]
		code.add(PushI,1);
		code.add(Add);
		code.add(StoreI);
		code.add(Jump,startLoop);
		code.add(Label,exitLoop);
		
		
		
		return code;
	}

}
