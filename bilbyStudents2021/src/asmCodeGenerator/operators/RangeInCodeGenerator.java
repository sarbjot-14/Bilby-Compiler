package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import java.util.List;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;

public class RangeInCodeGenerator implements SimpleCodeGenerator {
	private ASMOpcode subtractOpcode;
	private ASMOpcode jumpPosOpcode;
	private ASMOpcode dataOpcode;
	private ASMOpcode duplicateOpcode;
	private ASMOpcode storeOpcode;
	private ASMOpcode loadOpcode;
	private ASMOpcode jumpNegOpcode;

	


	public RangeInCodeGenerator(ASMOpcode subtractOpcode,ASMOpcode jumpPosOpcode,ASMOpcode duplicateOpcode ,
			ASMOpcode dataOpcode,ASMOpcode storeOpcode,ASMOpcode loadOpcode,ASMOpcode jumpNegOpcode  ) {
		super();
		this.subtractOpcode = subtractOpcode;
		this.jumpPosOpcode = jumpPosOpcode;
		this.dataOpcode = dataOpcode;
		this.duplicateOpcode = duplicateOpcode;
		this.storeOpcode = storeOpcode;
		this.loadOpcode = loadOpcode;
		this.jumpNegOpcode = jumpNegOpcode;

		
	}


	@Override
	public ASMCodeFragment generate(ParseNode node, List<ASMCodeFragment> args) {
	

		Labeller labeller = new Labeller("rangeIn");
		String startLabel = labeller.newLabel("start");
		String highLabel = labeller.newLabel("high");
		String lowLabel = labeller.newLabel("low");
		String subLabel   = labeller.newLabel("sub");
		String trueLabel  = labeller.newLabel("true");
		//String trueAndPopLabel  = labeller.newLabel("trueAndPop");
		String falseAndPopLabel = labeller.newLabel("falseAndPop");
		String falseLabel = labeller.newLabel("false");
		String joinLabel  = labeller.newLabel("join");

		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		code.add(Label, startLabel);
		for(ASMCodeFragment fragment: args) {
			code.append(fragment);
		}
		//((a >= low r) && (a <= high r))
		
		
		// store high
		code.add(DLabel,highLabel); // [&subTypeSize, &subTypeSize]
		if(dataOpcode == ASMOpcode.DataI) {
			code.add(dataOpcode, 0);
		}
		else {
			code.add(dataOpcode, 0.0);
		}
		//code.add(DataF,0.0);
		code.add(PushD, highLabel); // [&subTypeSize, &subTypeSize, &subTypeVar]
		code.add(Exchange); // [&subTypeSize, &subTypeVar, &subTypeSize]
		
		code.add(storeOpcode); //  [&subTypeSize]
		
		// store low
		code.add(DLabel,lowLabel); // [&subTypeSize, &subTypeSize]
		if(dataOpcode == ASMOpcode.DataI) {
			code.add(dataOpcode, 0);
		}
		else {
			code.add(dataOpcode, 0.0);
		}
		//code.add(DataF);
		code.add(PushD, lowLabel); // [&subTypeSize, &subTypeSize, &subTypeVar]
		code.add(Exchange); // [&subTypeSize, &subTypeVar, &subTypeSize]
		code.add(storeOpcode); //  [&subTypeSize]
		
		code.add(duplicateOpcode);
		code.add(PushD,lowLabel);
		code.add(loadOpcode);
		
		//((a >= low r) && (a <= high r))
		code.add(subtractOpcode);
		// if neg then false and pop
		code.add(jumpNegOpcode,falseAndPopLabel);
		// if 0 then true  .. let i slip through
		//code.add(jumpNegOpcode,falseLabel);
		
		code.add(PushD,highLabel);
		code.add(loadOpcode);
		code.add(subtractOpcode);
		// if negative then true, if 0 then true, if pos then false
		code.add(jumpPosOpcode,falseLabel);
		code.add(Jump,trueLabel);
	
		
		code.add(Label, trueLabel);
		code.add(PushI, 1);
		code.add(Jump, joinLabel);
		code.add(Label, falseLabel);
		code.add(PushI, 0);
		code.add(Jump, joinLabel);
		code.add(Label, falseAndPopLabel);
		code.add(Pop);
		code.add(PushI, 0);
		code.add(Jump, joinLabel);
		code.add(Label, joinLabel);
		
		
		return code;
	}

}
