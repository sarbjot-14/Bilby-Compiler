package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;


import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class FloatLeftRangeAdditionCodeGenerator implements SimpleCodeGenerator {
	


	public FloatLeftRangeAdditionCodeGenerator() {
		super();
	
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("rangeAdditionLeftFloat");

		String startLabel = labeller.newLabel("start");
		String additionOperandLabel   = labeller.newLabel("additionOperand");


		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}
		
		// store addition operand
		code.add(Duplicate);// [&subTypeSize, &subTypeSize]
		code.add(DLabel,additionOperandLabel); // [&subTypeSize, &subTypeSize]
		code.add(DataF, 0.0);
		code.add(PushD, additionOperandLabel); // [&subTypeSize, &subTypeSize, &subTypeVar]
		code.add(Exchange); // [&subTypeSize, &subTypeVar, &subTypeSize]
		code.add(StoreF); //  [&subTypeSize]
		
		code.add(FAdd);
		code.add(Exchange);
		code.add(PushD,additionOperandLabel);
		code.add(LoadF);
		code.add(FAdd);
		
		code.add(Exchange);
		
		return code;
	}

}
