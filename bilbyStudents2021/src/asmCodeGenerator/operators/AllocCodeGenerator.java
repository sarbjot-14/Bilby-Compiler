package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;
import static asmCodeGenerator.codeStorage.ASMOpcode.PStack;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpTrue;
import static asmCodeGenerator.codeStorage.ASMOpcode.DataI;
import static asmCodeGenerator.codeStorage.ASMOpcode.Call;
import static asmCodeGenerator.codeStorage.ASMOpcode.StoreI;
import static asmCodeGenerator.codeStorage.ASMOpcode.Duplicate;
import static asmCodeGenerator.codeStorage.ASMOpcode.Add;

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

public class AllocCodeGenerator implements SimpleCodeGenerator {

	



	public AllocCodeGenerator() {
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

		/*
		 * Code in 
		 * 
		 */
		// call memory manager
		// push size of record
		int size = node.getChildren().get(0).getType().getSize();
		IntegerConstantNode len = (IntegerConstantNode) node.getChildren().get(1); // fix for general expression
		
		int sizeOfRecord = 16 + len.getValue()*size;
		//call memory manager
		code.add(PushI,sizeOfRecord);
		code.add(Call,MemoryManager.MEM_MANAGER_ALLOCATE);
		
		// type identifier
		code.add(Duplicate);
		// [&record]
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
		code.add(PushI, len.getValue());
		code.add(StoreI);
		
		
		
		//Elements
		for(int i = 0; i< len.getValue(); i++) {
			code.add(Duplicate);
			code.add(PushI, 16+i*size);
			code.add(Add);
			code.add(PushI, 0);
			code.add(StoreI);
		}
		
		
		return code;
	}

}
