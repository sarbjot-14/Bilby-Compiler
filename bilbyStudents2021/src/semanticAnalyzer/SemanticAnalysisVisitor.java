package semanticAnalyzer;

import lexicalAnalyzer.Keyword;
import java.util.Arrays;
import java.util.List;

import inputHandler.Locator;

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
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.RangeNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.signatures.PromotedSignature;
import semanticAnalyzer.signatures.Promotion;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.PrimitiveType.*;
import semanticAnalyzer.types.Range;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
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
    		for(Promotion promotion:Promotion.values()) {
				if(promotion.applies(assignmentType)) {
					Type promotedType = promotion.apply(assignmentType);
					if(promotedType == identifier.getType()) {
						node.setType(identifier.getType());
						return;
						
					}
				}	
    		}
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
//			for(int i =0 ; i< promotedSignatures.size(); i++) {
//				System.out.println(promotedSignatures.get(i).promotions.get(0));
//				System.out.println(promotedSignatures.get(i).promotions.get(1));
//			}
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
		List <PrimitiveType> promotableTypes = new ArrayList <PrimitiveType>(Arrays.asList(PrimitiveType.CHARACTER,PrimitiveType.INTEGER,PrimitiveType.FLOAT));
		// check if no promotion is needed
		boolean hasSameType = true;
		Type firstType =  node.getChildren().get(0).getType();
		Array arrayType = null;

		// check if nested array
		if(!(firstType instanceof Array )) {
			for(ParseNode childNode:node.getChildren()) {
				//System.out.println(childNode.getType().concreteType());
				if(childNode.getType() instanceof Range) {
					Range firstRangeElement = (Range) node.getChildren().get(0).getType();
					Range rangeElement = (Range) childNode.getType();
					if(firstRangeElement.getSubtype() != rangeElement.getSubtype()) {
						hasSameType = false;
					}
				}
				else if(childNode.getType() != firstType) {
					hasSameType = false;
				}
			}
		}
		else { // is nested array
			// how to check if nested arrays are same?
		}
		
		// check if able to promote with least amount of promotions
		//PrimitiveType targetType = PrimitiveType.INTEGER; 
		if(!hasSameType) {
			for(PrimitiveType promotableType : promotableTypes) {
				//				System.out.println(promotableType);
				//				System.out.println("IS PROMOTABLE?");
				//				System.out.println(isArrayPromotable(node, promotableType));
				// if is promotable ,promote to that type and return 
				if(isArrayPromotable(node, promotableType)) {
					//System.out.println("FOUND A PROMOTABLE TYPE");

					Type subtype = promotableType;//node.child(0).getType();
					Type arrayT = new Array(subtype);
					node.setType(arrayT);
					return;
				}



				//System.out.println("NNNNNNNEXT");
			}
		}
		else {
			Type subtype = node.child(0).getType();
			Type arrayT = new Array(subtype);
			node.setType(arrayT);
			return;
		}


		//System.out.println("COULD NOT FIND ANY PROMOTABLE TYPE THROW ERROR");
		promotableArrayError(node);



	}

	private boolean isArrayPromotable(ArrayNode node, PrimitiveType targetType) {
		for(ParseNode element:node.getChildren()) {
			for(Promotion promotion: Promotion.values()) {
				//System.out.println(element.getType());
				//System.out.println(promotion);
				if(element.getType() == targetType) {
					//System.out.println("already target type, try next element");
					break;
				}

				//System.out.println(promotion);
				// if promotion on element creates target type and promotion is applicable on the element
				else if(promotion.apply(element.getType()) == targetType && promotion.applies( element.getType())) {
					//System.out.println("change to ");
					//System.out.println(promotion.apply(element.getType()));
					// save cast change then apply changes afterwards
					break;
				}
				else {

					// need to check if all prmotions checked for that element
					// if so then we know promotion on array for that target type is not possible
					Promotion lastTypeInPromotions = Arrays.asList(Promotion.values()).get(Arrays.asList(Promotion.values()).size() - 1);
					if(lastTypeInPromotions == promotion) {
						//System.out.println("Cannot change element to targetType:");
						//System.out.println(targetType);
						//System.out.println("try again with new targetType at beginning of elements");
						//break; // return with null or something
						return false;
					}

				}
			}
			
		}
		return true;
	}
	@Override
	public void visitLeave(RangeNode node) {
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
		else if(node.isRange()) {
			Type subType = node.child(0).getType();
			Type rangeType = new Range(subType);
			if(subType == PrimitiveType.INTEGER || subType == PrimitiveType.FLOAT || subType == PrimitiveType.CHARACTER ) {
				node.setType(rangeType);
			}
			else {
				invalidRangeTypeError(node,subType);
			}
			
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
	public void visit(BreakNode node) {
	}
	@Override
	public void visit(ContinueNode node) {
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