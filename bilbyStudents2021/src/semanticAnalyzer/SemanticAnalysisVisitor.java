package semanticAnalyzer;

import lexicalAnalyzer.Keyword;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import logging.BilbyLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.StatementBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.signatures.PromotedSignature;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(StatementBlockNode node) {
		enterSubscope(node);
	}
	public void visitLeave(StatementBlockNode node) {
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
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
	// statements and declarations
	@Override
	public void visitLeave(PrintStatementNode node) {
	}
	@Override
	public void visitLeave(DeclarationNode node) {
		boolean isImmutable=false;
		if(node.getToken().isLextant(Keyword.IMM)) {
			isImmutable= true;
		}
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode initializer = node.child(1);
		
		Type declarationType = initializer.getType();
		node.setType(declarationType);
		
		identifier.setType(declarationType);
		addBinding(identifier, declarationType, isImmutable);
	}
	@Override
	public void visitLeave(AssignmentNode node) {
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		if(identifier.getBinding().getIsImmutable()) {
			logError("assignment on immutable");
		}
		ParseNode initializer = node.child(1);

		Type assignmentType = initializer.getType();
		List<Type> typeList = new ArrayList<Type>();
        typeList.add(identifier.getType());
        typeList.add(assignmentType);

        if(identifier.getType() instanceof Array && assignmentType instanceof Array) {
        	Array assignmentArray = (Array) assignmentType;
        	Array identifierArray = (Array) identifier.getType();
        	if(assignmentArray.getSubtype() == identifierArray.getSubtype()) {
        		node.setType(assignmentType);
        	}
        	else {
        		typeCheckError(node,typeList);
        	}
        }
        else if(identifier.getType() == assignmentType) {
        	node.setType(assignmentType);
			
		}
        else {
        	typeCheckError(node,typeList);
        }
        

		
	}

	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(OperatorNode node) {
		List<Type> childTypes = childTypes(node);
		Lextant operator = operatorFor(node);
		
		FunctionSignatures signatures = FunctionSignatures.signaturesOf(operator);
		
		List<PromotedSignature> promotedSignatures = signatures.leastLevelPromotions(childTypes);
		
		if(promotedSignatures.isEmpty()){
			typeCheckError(node,childTypes);
			node.setType(PrimitiveType.ERROR);
			
		}
		else if(promotedSignatures.size() > 1){
			for(int i =0 ; i< promotedSignatures.size(); i++) {
//				System.out.println(promotedSignatures.get(i).promotions.get(0));
//				System.out.println(promotedSignatures.get(i).promotions.get(1));
			}
			multipleInterpretationError(node,childTypes);
			node.setType(PrimitiveType.ERROR);
			
		}
		else {
			PromotedSignature promotedSignature = promotedSignatures.get(0);
			node.setType(promotedSignature.resultType());
			node.setPromotedSignature(promotedSignature);
			
		}
		
	}


	private List<Type> childTypes(OperatorNode node) {
		List<Type> childTypes;  
		if(node.nChildren() == 1) {
			
			ParseNode child = node.child(0);
			childTypes = Arrays.asList(child.getType());
		}
		else {
			assert node.nChildren() == 2;
			ParseNode left  = node.child(0);
			ParseNode right = node.child(1);
			
			childTypes = Arrays.asList(left.getType(), right.getType());		
		}
		return childTypes;
	}
	@Override
	public void visitLeave(ArrayNode node) {
		Type subtype = node.child(0).getType();
		Type arrayType = new Array(subtype);
		node.setType(arrayType);

	}
	private Lextant operatorFor(OperatorNode node) {
		LextantToken token = (LextantToken) node.getToken();
		return token.getLextant();
	}
	
	
	@Override
	public void visitLeave(TypeNode node) {
		if(node.isArray()) {
			Type subtype = node.child(0).getType();
			Type arrayType = new Array(subtype);
			node.setType(arrayType);
		}
		else {
			node.setType(node.typeFromToken());
		}
		//-----
		//node.setType(PrimitiveType.fromToken(node.typeToken()));
		//-----
		//Primitive.fromToken
			// switch on lextant
			// key words : int, float, bool
		
		// Another way to do it is add contructor to Prmitive with keyword/lextant of that type
			// then search lextant from that token
			// basically loop through all prmitive types and see if type token is correct token
	}


	///////////////////////////////////////////////////////////////////////////
	// simple leaf nodes
	@Override
	public void visit(BooleanConstantNode node) {
		node.setType(PrimitiveType.BOOLEAN);
	}
	@Override
	public void visit(ErrorNode node) {
		node.setType(PrimitiveType.ERROR);
	}
	@Override
	public void visit(IntegerConstantNode node) {
		node.setType(PrimitiveType.INTEGER);
	}
	@Override
	public void visit(CharacterConstantNode node) {
		node.setType(PrimitiveType.CHARACTER);
	}
	@Override
	public void visit(StringConstantNode node) {
		node.setType(PrimitiveType.STRING);
	}
	@Override
	public void visit(FloatingConstantNode node) {
		node.setType(PrimitiveType.FLOAT);
	}
	@Override
	public void visit(NewlineNode node) {
	}
	@Override
	public void visit(SpaceNode node) {
	}
	@Override
	public void visit(TabNode node) {
	}
	@Override
	public void visitLeave(CastNode node) {
		
		List<Type> childTypes;  
		if(node.nChildren() == 1) {
			ParseNode child = node.child(0);
			childTypes = Arrays.asList(child.getType());
			
		}
		else {
			assert node.nChildren() == 2;
			ParseNode left  = node.child(0);
			ParseNode right = node.child(1);
			
			
			
			childTypes = Arrays.asList(left.getType(), right.getType());		
		}
		
		//Lextant operator = operatorFor(node);
		FunctionSignature signature = FunctionSignatures.signature(Punctuator.CAST, childTypes);
		
		if(signature.accepts(childTypes)) {
			node.setType(signature.resultType()); // why?
			node.setSignature(signature);
		}
		else {
			typeCheckError(node, childTypes);
			node.setType(PrimitiveType.ERROR);
		}
		
	}
	///////////////////////////////////////////////////////////////////////////
	// IdentifierNodes, with helper methods
	@Override
	public void visit(IdentifierNode node) {
		if(!isBeingDeclared(node)) {		
			Binding binding = node.findVariableBinding();
			
			node.setType(binding.getType());
			node.setBinding(binding);
		}
		// else parent DeclarationNode does the processing.
	}
	private boolean isBeingDeclared(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof DeclarationNode) && (node == parent.child(0));
	}
	private void addBinding(IdentifierNode identifierNode, Type type, boolean isImmutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type,isImmutable);
		identifierNode.setBinding(binding);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing

	private void multipleInterpretationError(OperatorNode node, List<Type> childTypes) {
		Token token = node.getToken();

		logError("operator " + token.getLexeme() + " has multiple interpretations " 
				+ childTypes  + " at " + token.getLocation());

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