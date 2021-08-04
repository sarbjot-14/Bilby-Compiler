package asmCodeGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.operators.SimpleCodeGenerator;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.BreakNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ContinueNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.ForNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocation;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfNode;
import parseTree.nodeTypes.IndexAssignmentNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.StatementBlockNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.RangeNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileNode;
import semanticAnalyzer.signatures.Promotion;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.FunctionSignatureType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Range;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeVariable;
import symbolTable.Binding;
import symbolTable.Scope;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

// do not call the code generator if any errors have occurred during analysis.
public class ASMCodeGenerator {
	ParseNode root;

	public static ASMCodeFragment generate(ParseNode syntaxTree) {
		ASMCodeGenerator codeGenerator = new ASMCodeGenerator(syntaxTree);
		return codeGenerator.makeASM();
	}
	public ASMCodeGenerator(ParseNode root) {
		super();
		this.root = root;
	}
	
	public ASMCodeFragment makeASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.append(MemoryManager.codeForInitialization());
		code.append( RunTime.getEnvironment() );
		code.append( globalVariableBlockASM() );
		code.append( programASM() );
		code.append( MemoryManager.codeForAfterApplication() );
		
		return code;
	}
	private ASMCodeFragment globalVariableBlockASM() {
		assert root.hasScope();
		Scope scope = root.getScope();
		int globalBlockSize = scope.getAllocatedSize();
		
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(DLabel, RunTime.GLOBAL_MEMORY_BLOCK);
		code.add(DataZ, globalBlockSize);
		return code;
	}
	private ASMCodeFragment programASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		
		code.add(    Label, RunTime.MAIN_PROGRAM_LABEL);
		code.append( programCode());
		code.add(    Halt );
		
		return code;
	}
	private ASMCodeFragment programCode() {
		CodeVisitor visitor = new CodeVisitor();
		root.accept(visitor);
		return visitor.removeRootCode(root);
	}


	protected class CodeVisitor extends ParseNodeVisitor.Default {
		private Map<ParseNode, ASMCodeFragment> codeMap;
		ASMCodeFragment code;
		
		public CodeVisitor() {
			codeMap = new HashMap<ParseNode, ASMCodeFragment>();
		}


		////////////////////////////////////////////////////////////////////
        // Make the field "code" refer to a new fragment of different sorts.
		private void newAddressCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_ADDRESS);
			codeMap.put(node, code);
		}
		private void newValueCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VALUE);
			codeMap.put(node, code);
		}
		private void newVoidCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VOID);
			codeMap.put(node, code);
		}

	    ////////////////////////////////////////////////////////////////////
        // Get code from the map.
		private ASMCodeFragment getAndRemoveCode(ParseNode node) {
			ASMCodeFragment result = codeMap.get(node);
			codeMap.remove(node);
			return result;
		}
	    public  ASMCodeFragment removeRootCode(ParseNode tree) {
			return getAndRemoveCode(tree);
		}		
		ASMCodeFragment removeValueCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			makeFragmentValueCode(frag, node);
			return frag;
		}		
		private ASMCodeFragment removeAddressCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isAddress();
			return frag;
		}		
		ASMCodeFragment removeVoidCode(ParseNode node) {
			
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isVoid();
			return frag;
		}
		
	    ////////////////////////////////////////////////////////////////////
        // convert code to value-generating code.
		private void makeFragmentValueCode(ASMCodeFragment code, ParseNode node) {
			assert !code.isVoid();
			
			if(code.isAddress()) {
				turnAddressIntoValue(code, node);
			}	
		}
		private void turnAddressIntoValue(ASMCodeFragment code, ParseNode node) {
			if(node.getType() == PrimitiveType.INTEGER) {
				code.add(LoadI);
			}	
			else if(node.getType() == PrimitiveType.FLOAT) {
				code.add(LoadF);
				
			}
			else if(node.getType() == PrimitiveType.BOOLEAN) {
				code.add(LoadC);
			}	
			else if(node.getType() == PrimitiveType.CHARACTER) {
				code.add(LoadC);
			}
			else if(node.getType() == PrimitiveType.STRING) {
				code.add(LoadI);

			}
			else if(node.getType() instanceof Array) {
				code.add(LoadI);

			}
			else if(node.getType() instanceof Range) {
				
				//Range rangeType = (Range)node.getType();
				Range rangeType = (Range) node.getType();
				//System.out.println("type in turnAddres to value is "+rangeType.getSubtype().concreteType());

				//Type rangeSubType = rangeType.concreteType();
				code.add(Duplicate);
				//System.out.println("turnAddressIntoValue");
				if(rangeType.getSubtype().concreteType()== PrimitiveType.INTEGER ) {
					code.add(LoadI);
					code.add(Exchange);
					code.add(PushI,rangeType.concreteType().getSize());
					code.add(Add);
					code.add(LoadI);
				}
				else if(rangeType.getSubtype().concreteType() == PrimitiveType.CHARACTER) {
					code.add(LoadC);
					code.add(Exchange);
					code.add(PushI,rangeType.concreteType().getSize());
					code.add(Add);
					code.add(LoadC);
				}
				else if(rangeType.getSubtype().concreteType() == PrimitiveType.FLOAT) {
					code.add(LoadF);
					code.add(Exchange);
					code.add(PushI,8);
					code.add(Add);
					code.add(LoadF);
				}
				
				
			
			}
			else {
				assert false : "node " + node;
			}
			code.markAsValue();
		}
		
	    ////////////////////////////////////////////////////////////////////
        // ensures all types of ParseNode in given AST have at least a visitLeave	
		public void visitLeave(ParseNode node) {
			assert false : "node " + node + " not handled in ASMCodeGenerator";
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructs larger than statements
		public void visitLeave(ProgramNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}
		public void visitLeave(StatementBlockNode node) {
			
			if(!(node.getParent() instanceof FunctionDefinitionNode)) {
				newVoidCode(node);
				for(ParseNode child : node.getChildren()) {
//					ASMCodeFragment childCode = removeVoidCode(child);
//					code.append(childCode);
					//
					if(child instanceof ReturnNode){
						// put return value on asm stack
						
						
						for(ParseNode current : node.pathToRoot()) {
							if(current instanceof FunctionDefinitionNode  ) {
								FunctionDefinitionNode funcDef = (FunctionDefinitionNode)current;
								//System.out.println(funcDef.getStartExistHandShakeLabel());
								code.append(removeValueCode(child.child(0)));
								code.add(Jump,funcDef.getStartExistHandShakeLabel());
								//code.add(Halt);
								break;
							}
							
						}	
						break;
						
					}
					else {
						
						ASMCodeFragment childCode = removeVoidCode(child);
						code.append(childCode);
					}
				}
				
			}
			
			
		}

		///////////////////////////////////////////////////////////////////////////
		// statements and declarations

		public void visitLeave(PrintStatementNode node) {
			newVoidCode(node);
			new PrintStatementGenerator(code, this).generate(node);	
		}
		public void visit(NewlineNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.NEWLINE_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(SpaceNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.SPACE_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(TabNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.TAB_PRINT_FORMAT);
			code.add(Printf);
		}
		

		public void visitLeave(DeclarationNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			code.append(lvalue);
			
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			code.append(rvalue);
			
			ASMCodeFragment storeFrag = generateStore(node);
			code.append(storeFrag);
	
			
		}
		public void visitLeave(AssignmentNode node) {
			
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));

			code.append(lvalue);
			code.append(rvalue);
			if(node.child(0).getType() != node.child(1).getType() ) {
				for(Promotion promotion:Promotion.values()) {
					if(promotion.applies(node.child(1).getType())) {
						Type promotedType = promotion.apply(node.child(1).getType());
						if(promotedType == node.child(0).getType()) {
							if(promotedType == PrimitiveType.FLOAT) {
								code.add(ConvertF);
							}
//							else if(promotedType == PrimitiveType.INTEGER) {
//								
//							}
							
						}
					}
				}
			}
			

			Type type = node.getType();
			ASMCodeFragment storeFrag = generateStore(node);
			code.append(storeFrag);
			
		}
		public void visitLeave(IndexAssignmentNode node) {
			
			newVoidCode(node);
			ASMCodeFragment lvalue = removeValueCode(node.child(0));	
			ASMCodeFragment index = removeValueCode(node.child(1));	
			ASMCodeFragment rvalue = removeValueCode(node.child(2));
			
			Labeller labeller = new Labeller("indexingAssign");
			
			String subTypeSize= labeller.newLabel("subTypeSize");
			String trueLabel  = labeller.newLabel("true");
			String joinLabel  = labeller.newLabel("join");
			
			// save the identifer
			String identifier = labeller.newLabel("identifier");
			code.add(DLabel, identifier);
			code.add(DataI, 0);
			code.add(PushD, identifier);
			code.append(lvalue); 
			code.add(StoreI); 
			
			// save the index
			String indexVar = labeller.newLabel("index");
			code.add(DLabel, indexVar);
			code.add(DataI, 0);
			code.add(PushD, indexVar);
			code.append(index); 
			code.add(StoreI); 
						

			
			code.add(PushD,identifier);
			code.add(LoadI);
	
			
			// get length for out of bounds check
			code.add(Duplicate);
			code.add(PushI, 12);
			code.add(Add);
			code.add(LoadI);
			// do runtime check for index
			code.add(PushD,indexVar);
			code.add(LoadI);
			code.add(JumpNeg,trueLabel); // if negative throw runtime error
			code.add(PushD,indexVar);
			code.add(LoadI);
			code.add(Subtract);
			code.add(Duplicate);
			code.add(JumpFalse,trueLabel);
			code.add(JumpNeg,trueLabel);

	
			code.add(Jump,joinLabel);
			code.add(Label, trueLabel);
			code.add(Jump, RunTime.INDEXING_RUNTIME_ERROR );
			code.add(Label, joinLabel);
			
		
			
			code.add(PushI, 8); 
			code.add(Add);
			code.add(Duplicate);
			code.add(LoadI);  // [&subTypeSize, subTypeSize]
		
			code.add(PushD,indexVar);
			code.add(LoadI); // [&subTypeSize, subTypeSize, index]
			code.add(Multiply);  // [&subTypeSize, byteToIndex]
			code.add(Exchange); 
			
			code.add(PushI,8); 
			code.add(Add);   // [ byteToIndex, &elements]
			code.add(Add); // [&indexedElement]
			code.append(rvalue);
	

			Type type = node.getChildren().get(2).getType();  
			code.add(opcodeForStore(type));
		}
		private ASMOpcode opcodeForLoad(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return LoadI;
			}
			if(type == PrimitiveType.FLOAT) {
				return LoadF;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return LoadI;
			}
			if(type == PrimitiveType.STRING) {
				return LoadI;
			}
			if(type == PrimitiveType.CHARACTER) {
				return LoadC;
			}
			if(type instanceof Array) {
				return LoadI;
			}
		
			
			assert false: "Type " + type + " unimplemented in opcodeForLoad()";
			return null;
		}
		private ASMOpcode opcodeForData(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return DataI;
			}
			if(type == PrimitiveType.FLOAT) {
				return DataF;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return DataI;
			}
			if(type == PrimitiveType.STRING) {
				return DataI;
			}
			if(type == PrimitiveType.CHARACTER) {
				return DataC;
			}
			if(type instanceof Array) {
				return DataI;
			}
		
			
			assert false: "Type " + type + " unimplemented in opcodeForData()";
			return null;
		}
		private ASMOpcode opcodeForStore(Type type) {
			if(type == PrimitiveType.INTEGER) {
				return StoreI;
			}
			if(type == PrimitiveType.FLOAT) {
				return StoreF;
			}
			if(type == PrimitiveType.BOOLEAN) {
				return StoreI;
			}
			if(type == PrimitiveType.STRING) {
				return StoreI;
			}
			if(type == PrimitiveType.CHARACTER) {
				return StoreC;
			}
			if(type instanceof Array) {
				return StoreI;
			}
		
			
			assert false: "Type " + type + " unimplemented in opcodeForStore()";
			return null;
		}
		
		ASMCodeFragment generateStore(ParseNode node){
			ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VOID);
			Type type = node.getType();
			
			if(type == PrimitiveType.INTEGER) {
				code.add(StoreI);
				return code;
			}
			if(type == PrimitiveType.FLOAT) {
				code.add(StoreF);
				return code;
			}
			if(type == PrimitiveType.BOOLEAN) {
				code.add(StoreI);
				return code;
			}
			if(type == PrimitiveType.STRING) {
				code.add(StoreI);
				return code;
			}
			if(type == PrimitiveType.CHARACTER) {
				code.add(StoreC);
				return code;
			}
			if(type instanceof Array) {
				code.add(StoreI);
				return code;
			}
			if(type instanceof Range) {
				
				Labeller labeller = new Labeller("rangeStore");

				String highendLabel = labeller.newLabel("highend");
				String lowendLabel = labeller.newLabel("lowend");
				Range rangeType = (Range) node.getType(); //
				Type subType = rangeType.getSubtype();
			
				//System.out.println(subType);
				//System.out.println("storing these");
				
				//System.out.println("subType in store is "+subType.concreteType());
			    if(subType.concreteType() == PrimitiveType.INTEGER ) {
					
					code.add(DLabel,highendLabel); // [&identifier, lowend, highend,]
					code.add(DataI, 0);
					code.add(PushD, highendLabel); // 
					code.add(Exchange); // [&identifier, lowend,&highend, highend]
					code.add(StoreI); // 
					code.add(Exchange); // [lowend, &identifier]
					code.add(Duplicate); // [lowend, &identifier, &identifier ]
					
					code.add(PushD,highendLabel);
					code.add(LoadI);
					code.add(StoreI);
					
					code.add(PushI,4);
					code.add(Add);
					code.add(Exchange);  // [&identifier+4,lowend ]
					code.add(StoreI);
					
					//System.out.println("Finished Storing");
					
				}
				else if(subType.concreteType() == PrimitiveType.FLOAT ) {
					
				    code.add(DLabel,highendLabel); // [&identifier, lowend, highend,]
					code.add(DataF, 0.0);
					code.add(PushD, highendLabel); // 
					code.add(Exchange); // [&identifier, lowend,&highend, highend]
					
					code.add(StoreF); // 
					code.add(Exchange); // [lowend, &identifier]
					code.add(Duplicate); // [lowend, &identifier, &identifier ]
					
					code.add(PushD,highendLabel);
					code.add(LoadF);
					code.add(StoreF);
					
					code.add(PushI,8);
					code.add(Add);
					code.add(Exchange);  // [&identifier+8,lowend ]
					code.add(StoreF);
					
				}
				else if(subType.concreteType() == PrimitiveType.CHARACTER ) {
					
					
					code.add(DLabel,highendLabel); // [&identifier, lowend, highend,]
					code.add(DataC, 0);
					code.add(PushD, highendLabel); // 
					code.add(Exchange); // [&identifier, lowend,&highend, highend]
					
					code.add(StoreC); // 
					code.add(Exchange); // [lowend, &identifier]
					code.add(Duplicate); // [lowend, &identifier, &identifier ]
					
					code.add(PushD,highendLabel);
					code.add(LoadC);
					code.add(StoreC);
					
					code.add(PushI,1);
					code.add(Add);
					code.add(Exchange);  // [&identifier+1,lowend ]
					code.add(StoreC);
					
				}
				
				return code;
			}
			
			assert false: "Type " + type + " unimplemented in opcodeForStore()";
			return null;
			
		}
		


		///////////////////////////////////////////////////////////////////////////
		// expressions
		public void visitLeave(OperatorNode node) {
			newValueCode(node);
			
			Object variant = node.getPromotedSignature().getVariant();

			if(variant instanceof ASMOpcode) {
				if(node.getChildren().size() >=1) {
					// First argument
					ASMCodeFragment arg = removeValueCode(node.getChildren().get(0));
					code.append(arg);
					Promotion promotionFirst = node.getPromotedSignature().promotions.get(0);
					ASMCodeFragment promoCode = promotionFirst.codeFor();
					code.append(promoCode);
					
				}
				if(node.getChildren().size() ==2) {
					// Second argument
					ASMCodeFragment argTwo = removeValueCode(node.getChildren().get(1));

					code.append(argTwo);
					Promotion promotionSecond = node.getPromotedSignature().promotions.get(1);
					ASMCodeFragment promoCodeTwo = promotionSecond.codeFor();
					code.append(promoCodeTwo);
					
					
				}
				code.add((ASMOpcode)variant);
				

			}
			
			else if(variant instanceof SimpleCodeGenerator) {
				SimpleCodeGenerator generator = (SimpleCodeGenerator)variant;
				List<ASMCodeFragment> args = new ArrayList<>();

				//First argument
				ASMCodeFragment arg = removeValueCode(node.getChildren().get(0));
				Promotion promotionFirst = node.getPromotedSignature().promotions.get(0);
				ASMCodeFragment promoCode = promotionFirst.codeFor();
				arg.append(promoCode);
				args.add(arg);
				
				if(node.getChildren().size() == 2) {
					//Second argument
					ASMCodeFragment arg2 = removeValueCode(node.getChildren().get(1));
					Promotion promotionSecond = node.getPromotedSignature().promotions.get(1);
					ASMCodeFragment promoCode2 = promotionSecond.codeFor();
					arg2.append(promoCode2);
					args.add(arg2);
				}
				
					
				
				
				ASMCodeFragment generated = generator.generate(node, args);
				code.appendWithCodeType(generated);
				
			}
			else {
				throw new RuntimeException("Varient unimplemented in ASMCodeGenerator Operator Nde");
			}

		}
		///////////////////////////////////////////////////////////////////////////
		// return
//		public void visitLeave(ReturnNode node) {
//			
//			//newVoidCode(node);
//			newValueCode(node);
//			code.append(removeValueCode(node));
//			
//
//
//		}
		///////////////////////////////////////////////////////////////////////////
		// function invocation
		public void visitLeave(FunctionInvocation node) {
			//newVoidCode(node);
			
			newValueCode(node); 
			// parameters are stored by the allocator?
			
			Labeller labeller = new Labeller("function-invocation");
			String startLabel = labeller.newLabel("start");
			String endLabel = labeller.newLabel("end");
			code.add(Label, startLabel);
			
			// store parameters?
			code.add(PushD,RunTime.STACK_POINTER);
			code.add(LoadI);
			
			for(ParseNode arg: node.child(1).getChildren()) {
				
				code.add(PushI,arg.getType().getSize());
				code.add(Subtract);
				code.add(Duplicate);

				code.append(removeValueCode(arg));
				code.add(opcodeForStore(arg.getType()) );
				
			}
			// update StackPointer
			code.add(PushD,RunTime.STACK_POINTER);
			//code.add(LoadI); // TODO: is this right?
			code.add(Exchange);
			code.add(StoreI);
			
			//System.out.println( node.child(0).getToken().getLexeme()+"-function-definition");
			
			code.add(Call, node.child(0).getToken().getLexeme()+"-function-definition");
			
			// check if frame pointer is the same...
//			code.add(PushD,RunTime.FRAME_POINTER); 
//			code.add(LoadI);  
//			code.add(Pop);
			// function then should take the return value from the location pointed at by the stack counter and 
			//place it on the ASM stack. 
			FunctionSignatureType sig = (FunctionSignatureType) node.child(0).getType();
			Type returnType = sig.returnType();
			
			code.add(PushD,RunTime.STACK_POINTER);
			code.add(LoadI);
			
			
			code.add(opcodeForLoad(returnType));
			//code.add(PStack);
			
			
			
			//Finally, it moves the stack pointer up by the size of the return value:
			code.add(PushD,RunTime.STACK_POINTER);
			code.add(Duplicate);
			code.add(LoadI);
			code.add(PushI,returnType.getSize());
			code.add(Add);
			code.add(StoreI);
			

		}
		///////////////////////////////////////////////////////////////////////////
		// function defintion
		public void visitEnter(FunctionDefinitionNode node) {
			newVoidCode(node);
			String startExitHandShakeLabel = node.child(1).getToken().getLexeme()+"-exit-hand-shake";
			node.setStartExistHandShakeLabel(startExitHandShakeLabel);
		}
		public void visitLeave(FunctionDefinitionNode node) {
			newVoidCode(node);
			//System.out.println(node.child(1).getToken().getLexeme()+"-function-definition");
			Labeller labeller = new Labeller("function-definition");
			String startLabel = node.child(1).getToken().getLexeme()+"-function-definition";
			String startExitHandShakeLabel = node.getStartExistHandShakeLabel();
			//node.setStartExistHandShakeLabel(startExitHandShakeLabel);
			
			String skipLabel = labeller.newLabel("skip");
			// need to skip over this code when visited
			
			code.add(Jump,skipLabel);
			code.add(Label, startLabel);
			// frame pointer
			
			// store frame pointer as dynamic link
			code.add(PushD,RunTime.STACK_POINTER); //[&address, STACK_POINTER]
			code.add(LoadI); //[&address,&STACK_POINTER]
			code.add(PushI,4);  
			code.add(Subtract);  // [&address,(&STACK_POINTER-4)]	
			code.add(Duplicate);// [&address,(&STACK_POINTER-4), (&STACK_POINTER-4)]	
			
			code.add(PushD,RunTime.FRAME_POINTER); // [&address,(&STACK_POINTER-4), (&STACK_POINTER-4), framePointer]
			code.add(LoadI);  
			code.add(StoreI); // [&address,(&STACK_POINTER-4)]
			// store return address
			code.add(PushI, 4);
			code.add(Subtract); // [&address,(&STACK_POINTER-4 -4)]
			code.add(Exchange);
			code.add(StoreI);
			
			// Frame pointer is set to same as Stack Pointer
			code.add(PushD,RunTime.FRAME_POINTER); 
			//code.add(LoadI);
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(StoreI);
			
			// size of the frame for barge() is subtracted from the stack pointer.
			Scope scope = node.child(3).getScope();
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(Duplicate);
			code.add(LoadI);
			code.add(PushI,scope.getAllocatedSize() );
			code.add(Subtract);
			code.add(StoreI);
			
			// execute procedure code 
			//Type returnType = null; //save type for later
			for(ParseNode child : node.child(3).getChildren()) { // TODO: What if return isnt last statement?
				if(child instanceof ReturnNode){
					// put return value on asm stack
					
					//returnType = child.child(0).getType();
					code.append(removeValueCode(child.child(0)));
					code.add(Jump,startExitHandShakeLabel);
					break;
					
				}
				else {
					ASMCodeFragment childCode = removeVoidCode(child);
					code.append(childCode);
				}
			}
			
			//exit handshake code for barge( ) should first push the return address (stored at framePointer-
			//8) onto the accumulator stack
			code.add(Label, startExitHandShakeLabel);
			
			code.add(PushD,RunTime.FRAME_POINTER); 
			code.add(LoadI);
			code.add(PushI, 8);
			code.add(Subtract);
			code.add(LoadI);
			
			
			//then replace the frame pointer with the dynamic link
			code.add(PushD,RunTime.FRAME_POINTER); 
			code.add(PushD,RunTime.FRAME_POINTER); 
			code.add(LoadI);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(LoadI);
			code.add(StoreI);
			
			//Exchange operation
			//brings the return value back to the top of the ASM accumulator stack.
			code.add(Exchange); //[&returnAddress, returnValue]
			
			
			//Then the code should increase the stack pointer by the size of barge's frame + the size of barge's arguments,
			//i.e. by the size of the parameter scope. Everything now below the stack pointer is no longer needed.
			code.add(PushD,RunTime.STACK_POINTER);
			code.add(Duplicate);
			code.add(LoadI);
			code.add(PushI, node.child(3).getScope().getAllocatedSize());
			code.add(Add);
			code.add(PushI,node.getScope().getAllocatedSize() );	
			code.add(Add);
			code.add(StoreI);
			
			//Finally, it should decrease the stack pointer by the return value size (in our example, the return value is an int,
			//having a size of 4 bytes).
			code.add(PushD,RunTime.STACK_POINTER);
			code.add(Duplicate);
			code.add(LoadI);
			code.add(PushI, node.child(0).getType().getSize());
			code.add(Subtract);
			code.add(StoreI);
			
			// Then it should store the return value at that location.
			code.add(PushD,RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(Exchange); //[&returnAddress, STACK_POINTER returnValue]
			//code.add(PStack);
			code.add(opcodeForStore(node.child(0).getType()));
			

			//return using the return address on the ASM stack, transferring control back 
			code.add(Return);	
			
			//code.add(Jump,"-function-invocation-2-end ");
			code.add(Label,skipLabel);
			

		}
		///////////////////////////////////////////////////////////////////////////
		// if else
		public void visitLeave(IfNode node) {
			newVoidCode(node);
			
			// push boolean conditional
			ParseNode booleanConditional = node.getChildren().get(0);
			ASMCodeFragment arg1 = removeValueCode(booleanConditional);
			code.append(arg1);
			
			// check conditional and jump over block statement
			String endIf = new Labeller("endIf").newLabel("");
			code.add(JumpFalse, endIf);

			// push block statement
			ParseNode blockStatement = node.getChildren().get(1);
			ASMCodeFragment arg2 =removeVoidCode(blockStatement);
			code.append(arg2);
			String endElse = new Labeller("endElse").newLabel("");
			code.add(Jump, endElse);
			
			
			// push end if label
			code.add(Label, endIf);	
			
			// add else block
			if(node.getChildren().size()==3) {
				ParseNode elseStatement = node.getChildren().get(2);
				ASMCodeFragment arg3 =removeVoidCode(elseStatement);
				code.append(arg3);
			}
			code.add(Label, endElse);

		}
		///////////////////////////////////////////////////////////////////////////
		// while
		public void visitEnter(WhileNode node) {
			Labeller labeller = new Labeller("whileLoop");
			String endLoop = labeller.newLabel("endLoop");
			node.setBreakLabel(endLoop);
			String startLoop = labeller.newLabel("startLoop");
			node.setContinueLabel(startLoop);
			
			
		}
		public void visitLeave(WhileNode node) {
			newVoidCode(node);
			//Labeller labeller = new Labeller("loop");

			String startLoop = node.getContinueLabel();
			String endLoop = node.getBreakLabel();
			
			// start of while condition
			//String startWhile = new Labeller("startWhile").newLabel("");
			code.add(Label, startLoop);	
			
			// check boolean conditional
			ParseNode booleanConditional = node.getChildren().get(0);
			ASMCodeFragment arg1 = removeValueCode(booleanConditional);
			code.append(arg1);
			
			// check conditional and jump over block statement
			//String endWhile = new Labeller("endWhile").newLabel("");
			code.add(JumpFalse, endLoop);
			
			// run block statement
			ParseNode blockStatement = node.getChildren().get(1);
			ASMCodeFragment arg2 =removeVoidCode(blockStatement);
			code.append(arg2);

			
			// jump to start of start of boolean condition
			code.add(Jump, startLoop);
			
			// end
			code.add(Label, endLoop);

		}
		///////////////////////////////////////////////////////////////////////////
		// for
		public void visitEnter(ForNode node) {
			newVoidCode(node);
			Labeller labeller = new Labeller("forLoop");
			String endLoop = labeller.newLabel("endLoop");
			node.setBreakLabel(endLoop);
			String startLoop = labeller.newLabel("startLoop");
			node.setContinueLabel(startLoop);
			String increment = labeller.newLabel("increment");
			node.setIncrementLabel(increment);
			
		}
		
		public void visitLeave(ForNode node) {
			newVoidCode(node);
			Labeller labeller = new Labeller("forLoop");

			String startLoop = node.getContinueLabel();
			String endLoop = node.getBreakLabel();
			String incrementLabel = node.getIncrementLabel();
			
			Type identifierType = node.getChildren().get(0).getChildren().get(0).getType();


			ASMCodeFragment rangeValues = removeValueCode(node.getChildren().get(0).getChildren().get(1));
			code.append(rangeValues);
			
			// save the high
			String high = labeller.newLabel("high");
			code.add(DLabel, high);
			code.add(opcodeForData(identifierType), 0);
			code.add(PushD, high);
			code.add(Exchange);
			code.add(opcodeForStore(identifierType)); 
			
			// save the low
			String count = labeller.newLabel("count");
			code.add(DLabel, count);
			code.add(opcodeForData(identifierType), 0);
			code.add(PushD, count);
			code.add(Exchange);
			code.add(opcodeForStore(identifierType)); 
			
			// update identifier
			String identifierAddress= labeller.newLabel("identifierAddress");
			code.add(DLabel, identifierAddress);
			code.add(DataI, 0);
			code.add(PushD, identifierAddress);
			ASMCodeFragment lvalue = removeAddressCode(node.getChildren().get(0).getChildren().get(0));	
			code.append(lvalue);
			code.add(StoreI);
			
			code.add(PushD,identifierAddress);
			code.add(LoadI);
			
			
			
			code.add(PushD,count);
			code.add(opcodeForLoad(identifierType));
			
			code.add(opcodeForStore(identifierType));
			
		
			
			
			code.add(Label, startLoop);	
			// check termination condition
			
			code.add(PushD,count);
			code.add(opcodeForLoad(identifierType));
			
			code.add(PushD,high);
			code.add(opcodeForLoad(identifierType));
			
			
			code.add(Subtract);
			code.add(JumpPos,endLoop);
			
			
			
			// run block statement
			ParseNode blockStatement = node.getChildren().get(1);
			ASMCodeFragment arg2 =removeVoidCode(blockStatement);
			code.append(arg2);

			code.add(Label, incrementLabel);
			// increment step
			code.add(PushD,count);
			code.add(opcodeForLoad(identifierType));
			code.add(PushI,1);
			code.add(Add);
			code.add(PushD, count);
			code.add(Exchange);
			
			code.add(opcodeForStore(identifierType));
			
			
			// update identifier
			code.add(PushD,identifierAddress);
			code.add(LoadI);
			
			code.add(PushD,count);
			code.add(opcodeForLoad(identifierType));
			
			
			code.add(opcodeForStore(identifierType));
			
//			// jump to start of start of boolean condition
			code.add(Jump, startLoop);

			// end
			code.add(Label, endLoop);

		}


		///////////////////////////////////////////////////////////////////////////
		// type casting
		public void visitLeave(TypeNode node) {
			newValueCode(node);
		}
		
		public void visitLeave(CastNode node) {
			//Now we just use the whichVariant field of the FunctionSignature for
			//a cast to hold the operation(s) required for the cast.
			newValueCode(node);
			
			Object variant = node.getSignature().getVariant();
			if(variant instanceof ASMOpcode) {
				for(ParseNode child:node.getChildren()) {
					ASMCodeFragment arg = removeValueCode(child);
					code.append(arg);
				}
				code.add((ASMOpcode)variant);
					
			}
			
			else if(variant instanceof SimpleCodeGenerator) {
				SimpleCodeGenerator generator = (SimpleCodeGenerator)variant;
				List<ASMCodeFragment> args = new ArrayList<>();
				for(ParseNode child:node.getChildren()) {
					ASMCodeFragment arg = removeValueCode(child);
					args.add(arg);
				}
				
				ASMCodeFragment generated = generator.generate(node, args);
				code.appendWithCodeType(generated);
				
			}
			else {
				throw new RuntimeException("Varient unimplemented in ASMCodeGenerator Operator Nde");
			}
		}
		public void visitLeave(ArrayNode node) {
			newValueCode(node);
			int arrayLength = node.getChildren().size();
			Type arrayType = ((Array)node.getType()).getSubtype();
			int subTypeSize = arrayType.getSize();
		
			if(arrayType instanceof Range) {
				subTypeSize = subTypeSize *2;
			}
			
			// push size of record
			code.add(PushI, arrayLength);
			code.add(PushI, subTypeSize);
			code.add(Multiply);
			code.add(PushI,16);
			code.add(Add);
			
			//call memory manager
			code.add(Call,MemoryManager.MEM_MANAGER_ALLOCATE);
			code.add(Duplicate);
			
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
			code.add(PushI, subTypeSize);
			code.add(StoreI);
			
			// Length
			code.add(Duplicate);
			code.add(PushI, 12);
			code.add(Add);
			code.add(PushI,arrayLength);
			code.add(StoreI);
			
			
			code.add(PushI, 16); // beginning of elements
			code.add(Add);
			code.add(Duplicate);
			
			
			List<ParseNode> elements = node.getChildren();
			//ParseNode elementNode = null;
			
			
			// do nothing if all in same type?
			for(int i = 0; i< arrayLength;i++) {
				code.append(removeValueCode(elements.get(i)));
				// if not matching with array type && NOT AN ARRAY && NOT A RANGE
				if(elements.get(i).getType() != arrayType && !(arrayType instanceof Array)&& !(arrayType instanceof Range)){

					if(elements.get(i).getType() == PrimitiveType.CHARACTER) {
						if(arrayType == PrimitiveType.INTEGER ) {
							// nothing?
							code.add(StoreI);
						}
						else if(arrayType == PrimitiveType.FLOAT) {
							code.add(ConvertF);
							code.add(StoreF);
							// add store code manually
						}
					}
					else if(elements.get(i).getType() == PrimitiveType.INTEGER) {
						if(arrayType == PrimitiveType.FLOAT) {
							code.add(ConvertF);
							code.add(StoreF);
							// add store code manually
						}
					}
				}
				else {
					code.append(generateStore(elements.get(i)));
				}
				
				if(elements.get(i).getType() instanceof Range) {
					Range rangeType = (Range) elements.get(i).getType() ;
					
					if(rangeType.getSubtype() == PrimitiveType.FLOAT) {
						code.add(PushI,16);
					}
					else if(rangeType.getSubtype() == PrimitiveType.INTEGER) {
						code.add(PushI,8);
					}
					else {
						assert(rangeType.getSubtype() == PrimitiveType.CHARACTER);
						code.add(PushI,2);
					}
					
					
				}
				else {
					code.add(PushI,subTypeSize); 
				}
				
				code.add(Add);
				code.add(Duplicate);
			}
			code.add(Pop);
			code.add(Pop);
			
			
		}
		///////////////////////////////////////////////////////////////////////////
		// leaf nodes (ErrorNode not necessary)
		public void visit(BooleanConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue() ? 1 : 0);
		}
		public void visit(IdentifierNode node) {
			newAddressCode(node);
			Binding binding = node.getBinding();
			
			binding.generateAddress(code);
		}		
		public void visit(IntegerConstantNode node) {
			newValueCode(node);
			
			code.add(PushI, node.getValue());
		}
		public void visit(FloatingConstantNode node) {
			newValueCode(node);
			
			code.add(PushF, node.getValue());
		}
		public void visit(CharacterConstantNode node) {
			newValueCode(node);
			
			code.add(PushI, node.getValue());
		}
		public void visit(BreakNode node) {
			newVoidCode(node);
			for(ParseNode current : node.pathToRoot()) {
				if(current instanceof WhileNode  ) {
					WhileNode whileNode = (WhileNode)current;
					code.add(Jump,whileNode.getBreakLabel());
				}
				else if(current instanceof ForNode) {
					ForNode forNode = (ForNode)current;
					code.add(Jump,forNode.getBreakLabel());
				}
			}	
			
		}
	
		public void visit(ContinueNode node) {
			newVoidCode(node);
			for(ParseNode current : node.pathToRoot()) {
				if(current instanceof WhileNode  ) {
					WhileNode whileNode = (WhileNode)current;
					code.add(Jump,whileNode.getContinueLabel());
				}
				else if(current instanceof ForNode) {
					ForNode forNode = (ForNode)current;
					code.add(Jump,forNode.getIncrementLabel());
				}
			}	
			
		}
		
		public void visit(StringConstantNode node) {
			newValueCode(node);
		
			String stringLabel = new Labeller("stringConstant").newLabel("");
			code.add(DLabel,stringLabel);
			code.add(DataI, 3);
			code.add(DataI, 9);
			code.add(DataI, node.getValue().length());
			code.add(DataS, node.getValue());
			code.add(PushD, stringLabel );
			
			
//			 Steps 1-3 can be done with DataI, ---> 3 and 9 and len
//			 Step 4 with DataS, and -- >  save characters including null
//			 Step 5 with a PushD (if you've labelled the data with a DLabel).   
//			 Step 5 is not a "return" but rather leaving the data address on the stack.
		}
		
	
	}

}
