package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpTrue;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.Printf;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushD;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;
import static asmCodeGenerator.codeStorage.ASMOpcode.Add;
import static asmCodeGenerator.codeStorage.ASMOpcode.DLabel;
import static asmCodeGenerator.codeStorage.ASMOpcode.PStack;
import static asmCodeGenerator.codeStorage.ASMOpcode.DataI;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import parseTree.ParseNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import asmCodeGenerator.ASMCodeGenerator.CodeVisitor;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.runtime.RunTime;

public class PrintStatementGenerator {
	ASMCodeFragment code;
	ASMCodeGenerator.CodeVisitor visitor;
	
	
	public PrintStatementGenerator(ASMCodeFragment code, CodeVisitor visitor) {
		super();
		this.code = code;
		this.visitor = visitor;
	}

	public void generate(PrintStatementNode node) {
		for(ParseNode child : node.getChildren()) {
			if(child instanceof NewlineNode || child instanceof SpaceNode || child instanceof TabNode) {
				ASMCodeFragment childCode = visitor.removeVoidCode(child);
				code.append(childCode);
			}
			else {
				appendPrintCode(child);
			}
		}
	}

	private void recursivePrintCode(Type type) {
		System.out.println("beginning of recursive call "+type);
		if(type instanceof Array) {

			//code.append(visitor.removeValueCode(node)); // address already there...
			convertToPointerIfArray(type);

			Labeller labeller = new Labeller("print-array-recursive");

			String lenStorage= labeller.newLabel("storage-for-arrayLength");
			String typeSizeStorage= labeller.newLabel("storage-for-subTypeSize");
			String startLoop = labeller.newLabel("start-loop-print");
			String exitLoop = labeller.newLabel("exit-loop-print");
			String counter = labeller.newLabel("counter-print");
			String skipCharPrint = labeller.newLabel("skip-char-print");
			String skipPop = labeller.newLabel("skip-pop");

			// [&subtypeSize]
			// store subtype size
			code.add(Duplicate);// [&subtypeSize, &subtypeSize]
			code.add(LoadI); // [&subtypeSize, subtypeSize]
			code.add(DLabel, typeSizeStorage);
			code.add(DataI, 0);
			code.add(PushD,typeSizeStorage);
			code.add(Exchange);  // [&subtypeSize,  "storage-for-subTypeSize", subtypeSize]
			//code.add(PStack);
			code.add(StoreI);  // [&subtypeSize]


			// store counter
			code.add(DLabel, counter);
			code.add(DataI,0);
			code.add(PushD,counter);
			code.add(PushI,0);
			code.add(StoreI);

			// store len
			code.add(PushI,4); // correct way to store len?
			code.add(Add);
			code.add(Duplicate);// [&len, &len]
			code.add(LoadI); // [&len, len]
			code.add(DLabel, lenStorage);
			code.add(DataI,0);
			code.add(PushD, lenStorage); // [&len, len, &lenStorage]
			code.add(Exchange);  // [&len,  "storage-for-arrayLength", len]
			code.add(StoreI);  // [&len]


			// print open brace
			code.add(PushI, 91); 
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);


			// loop and print
			code.add(PushI,4);
			code.add(Add); // start at first element
			code.add(Label,startLoop);
			code.add(PushD,lenStorage);
			code.add(LoadI);
			code.add(PushD,counter);
			code.add(LoadI);
			//code.add(PStack);
			code.add(Subtract);
			code.add(JumpFalse,exitLoop);


			// print element
			code.add(Duplicate); //[&len,&len]   (one less than start of elements in record
			code.add(PushD,typeSizeStorage);  
			code.add(LoadI); // [&len,&len, typeSize] 
			code.add(PushD,counter);  
			code.add(LoadI);   // [&len,&len, typeSize, counter] 
			//code.add(PStack);
			code.add(Multiply);
			code.add(Add); // [&len,&elemPos] 
			//code.add(PStack);


			Array myArray = (Array)type;
			//System.out.println(myArray.getSubtype());
			if(myArray.getSubtype() instanceof Array) {
				System.out.println("recursive call of codeAppend");
				code.add(LoadI); // address of a array
				//System.out.println(subArray.getSubtype());
				recursivePrintCode(myArray.getSubtype());
			}
			else if(myArray.getSubtype() ==PrimitiveType.BOOLEAN) {
				code.add(LoadC);
				//code.add(PStack);
				convertToStringIfBoolean(myArray.getSubtype());
				code.add(PushD, RunTime.BOOLEAN_PRINT_FORMAT);
				code.add(Printf);

			}
			else if(myArray.getSubtype() ==PrimitiveType.FLOAT) {
				code.add(LoadF);
				code.add(PushD, RunTime.FLOATING_PRINT_FORMAT);
				code.add(Printf);

			}
			else if(myArray.getSubtype() ==PrimitiveType.CHARACTER) {
				code.add(LoadC);
				code.add(Duplicate);
				code.add(JumpFalse,skipCharPrint);

				//code.add(PStack);
				code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
				code.add(Printf);
				code.add(Jump,skipPop);
				code.add(Label,skipCharPrint);
				code.add(Pop);
				code.add(Label,skipPop);


			}
			else if(myArray.getSubtype() ==PrimitiveType.STRING) {
				code.add(LoadI);
				convertToPointerIfString(myArray.getSubtype());
				code.add(PushD, RunTime.STRING_PRINT_FORMAT);
				code.add(Printf);

			}
			else if(myArray.getSubtype() ==PrimitiveType.INTEGER) {
				System.out.println("finally at a int");
				code.add(LoadI);
				code.add(PushD, RunTime.INTEGER_PRINT_FORMAT);
				code.add(Printf);

			}

			// update len
			code.add(PushD,counter);
			code.add(Duplicate); // [&lenStorage, &counter]
			code.add(LoadI); // [&lenStorage, counter]
			code.add(PushI,1);
			code.add(Add);
			code.add(StoreI);

			// print comma
			code.add(PushD,lenStorage);
			code.add(LoadI);
			code.add(PushD,counter);
			code.add(LoadI);
			code.add(Subtract);
			code.add(JumpFalse,exitLoop);
			code.add(PushI, 44);
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);
			code.add(PushI, 32); // space
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);


			code.add(Jump,startLoop);
			code.add(Label,exitLoop);


			// print close brace
			code.add(PushI, 93);
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);
			code.add(Pop);

		
		}
//		else {
//			String format = printFormat(type);
//
//			code.append(visitor.removeValueCode(node));
//			convertToStringIfBoolean(node.getType());
//			convertToPointerIfString(node.getType());
//			code.add(PushD, format);
//			code.add(Printf);
//		}	
		
	}
	private void appendPrintCode(ParseNode node) {
		System.out.println("first call to print");
		
		if(node.getType() instanceof Array) {
		    
			code.append(visitor.removeValueCode(node));
			convertToPointerIfArray(node.getType());
			
			Labeller labeller = new Labeller("print-array");
			
			String lenStorage= labeller.newLabel("storage-for-arrayLength");
			String typeSizeStorage= labeller.newLabel("storage-for-subTypeSize");
			String startLoop = labeller.newLabel("start-loop-print");
			String exitLoop = labeller.newLabel("exit-loop-print");
			String counter = labeller.newLabel("counter-print");
			String skipCharPrint = labeller.newLabel("skip-char-print");
			String skipPop = labeller.newLabel("skip-pop");
			
			// [&subtypeSize]
			// store subtype size
			code.add(Duplicate);// [&subtypeSize, &subtypeSize]
			code.add(LoadI); // [&subtypeSize, subtypeSize]
			code.add(DLabel, typeSizeStorage);
			code.add(DataI, 0);
			code.add(PushD,typeSizeStorage);
			code.add(Exchange);  // [&subtypeSize,  "storage-for-subTypeSize", subtypeSize]
			//code.add(PStack);
			code.add(StoreI);  // [&subtypeSize]
			
			
			// store counter
			code.add(DLabel, counter);
			code.add(DataI,0);
			code.add(PushD,counter);
			code.add(PushI,0);
			code.add(StoreI);
			
			// store len
			code.add(PushI,4); // correct way to store len?
			code.add(Add);
			code.add(Duplicate);// [&len, &len]
			code.add(LoadI); // [&len, len]
			code.add(DLabel, lenStorage);
			code.add(DataI,0);
			code.add(PushD, lenStorage); // [&len, len, &lenStorage]
			code.add(Exchange);  // [&len,  "storage-for-arrayLength", len]
			code.add(StoreI);  // [&len]
			
						
			// print open brace
			code.add(PushI, 91); 
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);
			
			
			// loop and print
			code.add(PushI,4);
			code.add(Add); // start at first element
			code.add(Label,startLoop);
			code.add(PushD,lenStorage);
			code.add(LoadI);
			code.add(PushD,counter);
			code.add(LoadI);
			//code.add(PStack);
			code.add(Subtract);
			code.add(JumpFalse,exitLoop);
			
			
			// print element
			code.add(Duplicate); //[&len,&len]   (one less than start of elements in record
			code.add(PushD,typeSizeStorage);  
			code.add(LoadI); // [&len,&len, typeSize] 
			code.add(PushD,counter);  
			code.add(LoadI);   // [&len,&len, typeSize, counter] 
			//code.add(PStack);
			code.add(Multiply);
			code.add(Add); // [&len,&elemPos] 
			//code.add(PStack);
			
			
			Array myArray = (Array)node.getType();
			//System.out.println(myArray.getSubtype());
			if(myArray.getSubtype() instanceof Array) {
				code.add(LoadI); // address of a array
				//System.out.println(subArray.getSubtype());
				recursivePrintCode(myArray.getSubtype());
			}
			else if(myArray.getSubtype() ==PrimitiveType.BOOLEAN) {
				code.add(LoadC);
				//code.add(PStack);
				convertToStringIfBoolean(myArray.getSubtype());
				code.add(PushD, RunTime.BOOLEAN_PRINT_FORMAT);
				code.add(Printf);
				
			}
			else if(myArray.getSubtype() ==PrimitiveType.FLOAT) {
				code.add(LoadF);
				code.add(PushD, RunTime.FLOATING_PRINT_FORMAT);
				code.add(Printf);
				
			}
			else if(myArray.getSubtype() ==PrimitiveType.CHARACTER) {
				code.add(LoadC);
				code.add(Duplicate);
				code.add(JumpFalse,skipCharPrint);
				
				//code.add(PStack);
				code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
				code.add(Printf);
				code.add(Jump,skipPop);
				code.add(Label,skipCharPrint);
				code.add(Pop);
				code.add(Label,skipPop);
				
				
			}
			else if(myArray.getSubtype() ==PrimitiveType.STRING) {
				code.add(LoadI);
				convertToPointerIfString(myArray.getSubtype());
				code.add(PushD, RunTime.STRING_PRINT_FORMAT);
				code.add(Printf);
				
			}
			else if(myArray.getSubtype() ==PrimitiveType.INTEGER) {
				code.add(LoadI);
				code.add(PushD, RunTime.INTEGER_PRINT_FORMAT);
				code.add(Printf);
				
			}
			
			// update len
			code.add(PushD,counter);
			code.add(Duplicate); // [&lenStorage, &counter]
			code.add(LoadI); // [&lenStorage, counter]
			code.add(PushI,1);
			code.add(Add);
			code.add(StoreI);
			
			// print comma
			code.add(PushD,lenStorage);
			code.add(LoadI);
			code.add(PushD,counter);
			code.add(LoadI);
			code.add(Subtract);
			code.add(JumpFalse,exitLoop);
			code.add(PushI, 44);
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);
			code.add(PushI, 32); // space
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);
			
			
			code.add(Jump,startLoop);
			code.add(Label,exitLoop);
			
			
			// print close brace
			code.add(PushI, 93);
			code.add(PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(Printf);
			code.add(Pop);
			
			
		}
		else {
			String format = printFormat(node.getType());

			code.append(visitor.removeValueCode(node));
			convertToStringIfBoolean(node.getType());
			convertToPointerIfString(node.getType());
			code.add(PushD, format);
			code.add(Printf);
		}		
		
		System.out.println("did the recursion end?");
	}
	private void convertToStringIfBoolean(Type type) {
		if(type != PrimitiveType.BOOLEAN) {
			return;
		}
		
		Labeller labeller = new Labeller("print-boolean");
		String trueLabel = labeller.newLabel("true");
		String endLabel = labeller.newLabel("join");
		code.add(JumpTrue, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_FALSE_STRING);
		code.add(Jump, endLabel);
		code.add(Label, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_TRUE_STRING);
		code.add(Label, endLabel);
	}
	private void convertToPointerIfString(Type type) {
		if(type != PrimitiveType.STRING) {
			return;
		}
		// add 12 bytes to arg which is on stack
		code.add(PushI, 12);
		code.add(Add);
		
	}
	
	private void convertToPointerIfArray(Type type) {
		if(! (type instanceof Array)) {
			return;
		}
		
		// add 8 which points to Subtype size
		code.add(PushI, 8);
		code.add(Add);
		
	}


	private static String printFormat(Type type) {
		
		
		if(type instanceof Array) {
			return RunTime.ARRAY_PRINT_FORMAT;
		}
		else {
			assert type instanceof PrimitiveType;
			switch((PrimitiveType)type) {
			case INTEGER:	return RunTime.INTEGER_PRINT_FORMAT;
			case FLOAT:	return RunTime.FLOATING_PRINT_FORMAT;
			case BOOLEAN:	return RunTime.BOOLEAN_PRINT_FORMAT;
			case CHARACTER:	return RunTime.CHARACTER_PRINT_FORMAT;
			case STRING:	return RunTime.STRING_PRINT_FORMAT;
			
			default:		
				assert false : "Type " + type + " unimplemented in PrintStatementGenerator.printFormat()";
				return "";
			}
		}
		
	}
}
