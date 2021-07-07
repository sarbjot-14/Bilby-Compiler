package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;


import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class FloatRightRangeAdditionCodeGenerator implements SimpleCodeGenerator {
	


	public FloatRightRangeAdditionCodeGenerator() {
		super();
	
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("rangeAdditionRightFloat");

		String startLabel = labeller.newLabel("start");
		String highLabel   = labeller.newLabel("high");
		String lowLabel   = labeller.newLabel("low");


		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}
		//code.add(Pop);
		
		// store high
		//code.add(Duplicate);// [&subTypeSize, &subTypeSize]
		code.add(DLabel,highLabel); // [&subTypeSize, &subTypeSize]
		code.add(DataF, 0.0);
		code.add(PushD, highLabel); // [&subTypeSize, &subTypeSize, &subTypeVar]
		code.add(Exchange); // [&subTypeSize, &subTypeVar, &subTypeSize]
		code.add(StoreF); //  [&subTypeSize]
		
		// store low
		//code.add(Duplicate);// [&subTypeSize, &subTypeSize]
		code.add(DLabel,lowLabel); // [&subTypeSize, &subTypeSize]
		code.add(DataF, 0.0);
		code.add(PushD, lowLabel); // [&subTypeSize, &subTypeSize, &subTypeVar]
		code.add(Exchange); // [&subTypeSize, &subTypeVar, &subTypeSize]
		code.add(StoreF); //  [&subTypeSize]
		
		code.add(Duplicate);
		code.add(PushD,highLabel);
		code.add(LoadF);
		code.add(FAdd);
		
		code.add(Exchange);
		code.add(PushD,lowLabel);
		code.add(LoadF);
		code.add(FAdd);
		
		code.add(Exchange);

		
		
//		
//		code.add(Add);
//		code.add(Exchange);
//		code.add(PushD,additionOperandLabel);
//		code.add(LoadI);
//		code.add(Add);
//		
//		code.add(Exchange);
		
		return code;
	}

}
