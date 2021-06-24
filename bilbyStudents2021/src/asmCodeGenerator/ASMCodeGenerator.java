package asmCodeGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.SimpleCodeGenerator;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.StatementBlockNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
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
				code.add(LoadI);
			}
			else if(node.getType() == PrimitiveType.STRING) {
				code.add(LoadI);

			}
			else if(node.getType() instanceof Array) {
				code.add(LoadI);

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
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
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
			ASMCodeFragment rvalue = removeValueCode(node.child(1));
			
			code.append(lvalue);
			code.append(rvalue);
			
			Type type = node.getType();
			code.add(opcodeForStore(type));
		}
		public void visitLeave(AssignmentNode node) {
			// refactor?
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
			ASMCodeFragment rvalue = removeValueCode(node.child(1));

			code.append(lvalue);
			code.append(rvalue);

			Type type = node.getType();
			code.add(opcodeForStore(type));
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


		///////////////////////////////////////////////////////////////////////////
		// expressions
		public void visitLeave(OperatorNode node) {
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
		public void visitLeave(WhileNode node) {
			newVoidCode(node);

			// start of while condition
			String startWhile = new Labeller("startWhile").newLabel("");
			code.add(Label, startWhile);	
			
			// check boolean conditional
			ParseNode booleanConditional = node.getChildren().get(0);
			ASMCodeFragment arg1 = removeValueCode(booleanConditional);
			code.append(arg1);
			
			// check conditional and jump over block statement
			String endWhile = new Labeller("endWhile").newLabel("");
			code.add(JumpFalse, endWhile);
			
			// run block statement
			ParseNode blockStatement = node.getChildren().get(1);
			ASMCodeFragment arg2 =removeVoidCode(blockStatement);
			code.append(arg2);

			
			// jump to start of start of boolean condition
			code.add(Jump, startWhile);
			
			// end
			code.add(Label, endWhile);

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
			int subTypeSize = node.child(0).getType().getSize();
			
			// push size of record
			code.add(PushI, arrayLength);
			code.add(PushI, subTypeSize);
			code.add(Multiply);
			code.add(PushI,16);
			code.add(Add);
			
			//call memory manager
			code.add(Call,MemoryManager.MEM_MANAGER_ALLOCATE);
			code.add(Duplicate);
			//code.add(PStack);
			
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
			for(int i = 0; i< arrayLength;i++) {
				//code.add(PStack);
				code.append(removeValueCode(elements.get(i)));
				
				//code.add(StoreI); // fix this
				code.add(opcodeForStore(node.getChildren().get(1).getType()));
				code.add(PushI,subTypeSize); 
				code.add(Add);
				code.add(Duplicate);
			}
			code.add(Pop);
			code.add(Pop);
			//code.add(PStack);
			
			
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
