package semanticAnalyzer;

import lexicalAnalyzer.Keyword;
import java.util.Arrays;
import java.util.List;

import inputHandler.Locator;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;

import java.util.ArrayList;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import logging.BilbyLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.BreakNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ContinueNode;
import parseTree.nodeTypes.StatementBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.ForNode;
import parseTree.nodeTypes.ForNodeX;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocation;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.ParameterListNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.RangeNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.signatures.PromotedSignature;
import semanticAnalyzer.signatures.Promotion;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.FunctionSignatureType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.PrimitiveType.*;
import semanticAnalyzer.types.Range;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.NegativeMemoryAllocator;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	
	@Override
	public void visitEnter(FunctionDefinitionNode node) {
		//enterParameterScope(node); // dont do this
		// set the function binding
				IdentifierNode identifier = (IdentifierNode) node.child(1);
				Type returnType = ((TypeNode)node.child(0)).typeFromToken();
				
				List<Type> paramTypeList = new ArrayList<Type>();
				TypeNode typeNode = null;
				for(ParseNode nodeChild:node.child(2).getChildren()) {
					typeNode = (TypeNode) nodeChild.child(0);
					//System.out.println(typeNode.typeFromToken());
					paramTypeList.add(typeNode.typeFromToken());
					//System.out.println(nodeChild.child(0).getToken());
					//System.out.println(nodeChild.child(0));
				}
				FunctionSignatureType functionSignatureType = new FunctionSignatureType(returnType,paramTypeList);
				//System.out.println(functionSignatureType);
				boolean isImmutable=true;
				addGlobalBinding(identifier, functionSignatureType, isImmutable);
				//addBinding(identifier, functionSignatureType, isImmutable);
				identifier.setType(functionSignatureType);
	}

	@Override
	public void visitLeave(FunctionDefinitionNode node) {
		//leaveScope(node); // leave parameter scope
		
		

	}
//	@Override
//	public void visitEnter(ReturnNode node) {
//		 
//	}
//

	
	// binding
	private void addGlobalBinding(IdentifierNode identifierNode, Type type, boolean isImmutable) {
		Scope scope = identifierNode.getGlobalScope();
		Binding binding = scope.createBinding(identifierNode, type,isImmutable);
		identifierNode.setBinding(binding);
	}
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}	
	private void enterParameterScope(ParseNode node) {
		Scope scope = Scope.createParameterScope();
		node.setScope(scope);
	}
	private void enterProcedureScope(ParseNode node) {
		Scope scope = Scope.createProcedureScope();
		node.setScope(scope);
	}
	@SuppressWarnings("unused")
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing

	private void multipleInterpretationError(OperatorNode node, List<Type> childTypes) {
		Token token = node.getToken();
		logError("operator " + token.getLexeme() + " has multiple interpretations " 
				+ childTypes  + " at " + token.getLocation());
	}
	private void invalidReturntypeError(ParseNode node, Type funcType, Type returnType) {
		Token token = node.getToken();
		logError("return type  has invalid type " 
				+ funcType  + " and " + returnType + " at " + token.getLocation());
	}
	private void invalidRangeTypeError(ParseNode node, Type type) {
		Token token = node.getToken();
		logError("operator " + token.getLexeme() + " range has invalid type " 
				+ type  + " at " + token.getLocation());
	}
	private void promotableArrayError(ParseNode node) {
		Token token = node.getToken();
		
		logError("array cannot be promoted to a common type");	
	}
	private void typeCheckError(ParseNode node, List<Type> operandTypes) {
		Token token = node.getToken();
		
		logError("operator " + token.getLexeme() + " not defined for types " 
				 + operandTypes  + " at " + token.getLocation());	
	}
	private void logError(String message) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
}