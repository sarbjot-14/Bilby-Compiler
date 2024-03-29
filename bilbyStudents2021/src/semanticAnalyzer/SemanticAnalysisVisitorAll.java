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
import symbolTable.MemoryLocation;
import symbolTable.NegativeMemoryAllocator;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;

class SemanticAnalysisVisitorAll extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
//	public void visitEnter(ProgramNode node) {
//		enterProgramScope(node);
//	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(StatementBlockNode node) {
		if(node.getParent() instanceof FunctionDefinitionNode) {
			enterProcedureScope(node);
			// allocate space for dynamic link and return addresss
			Scope s = node.getScope();
			NegativeMemoryAllocator allocator = (NegativeMemoryAllocator) s.getAllocationStrategy();
			allocator.allocate(PrimitiveType.INTEGER.getSize());
			allocator.allocate(PrimitiveType.INTEGER.getSize());
		}
		else {
			enterSubscope(node);
		}
		
	}
	public void visitLeave(StatementBlockNode node) {
		leaveScope(node);
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
			
				// if is promotable ,promote to that type and return 
				if(isArrayPromotable(node, promotableType)) {
					

					Type subtype = promotableType;//node.child(0).getType();
					Type arrayT = new Array(subtype);
					node.setType(arrayT);
					return;
				}

			}
		}
		else {
			Type subtype = node.child(0).getType();
			Type arrayT = new Array(subtype);
			node.setType(arrayT);
			return;
		}


		promotableArrayError(node);



	}

	private boolean isArrayPromotable(ArrayNode node, PrimitiveType targetType) {
		for(ParseNode element:node.getChildren()) {
			for(Promotion promotion: Promotion.values()) {
			
				if(element.getType() == targetType) {
					break;
				}

				// if promotion on element creates target type and promotion is applicable on the element
				else if(promotion.apply(element.getType()) == targetType && promotion.applies( element.getType())) {
				
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
	public void visitEnter(FunctionInvocation node) {
		
	}
	@Override
	public void visitLeave(FunctionInvocation node) {
		// get binding and check if formals match actuals

		IdentifierNode ident = (IdentifierNode) node.getChildren().get(0);
		Binding binding = ident.findVariableBinding();

		FunctionSignatureType functionSignatureType = (FunctionSignatureType) binding.getType();
		Type[] formals = functionSignatureType.getParamTypes();
		List<ParseNode> actuals = node.child(1).getChildren();
		if(formals == null ) {
			if( actuals.size() != 0) {
				incorrectNumberOfParametersError(node,formals);
				
			}
			
		}
		else if(formals.length != actuals.size()) {
			incorrectNumberOfParametersError(node,formals);
		}
		else {
			for(int i=0; i<formals.length ;i++) {
				if(formals[i] != actuals.get(i).getType()) {
					parameterExpressionListTypeError(node, formals[i], actuals.get(i).getType());
					//System.out.println("Acutals and Formals not same Type!!!!!");
				}
			}
		}
		// set binding and type on identifier
		ident.setBinding(binding);
		//ident.setType(functionSignatureType);
		// set type on functionInvocation node
		node.setType(functionSignatureType.returnType());




	}

	@Override
	public void visitLeave(ReturnNode node) {
		
		node.setType(node.child(0).getType());
		//System.out.println(node.child(0));
		for(ParseNode current : node.pathToRoot()) {
			if(current instanceof FunctionDefinitionNode  ) {
				FunctionDefinitionNode funcDef = (FunctionDefinitionNode)current;
				Type funcType = funcDef.child(0).getType();
				if(funcType != node.getType()) {
					
					invalidReturntypeError(node,funcType, node.getType());
				}

				break;
			}
			else if(current instanceof ProgramNode) {
				returnNotInFunctionError(node);
			}

		}
	}
	@Override
	public void visitEnter(FunctionDefinitionNode node) {
		enterParameterScope(node); 
	}

	@Override
	public void visitLeave(FunctionDefinitionNode node) {
		leaveScope(node);
	}
	
	@Override
	public void visitEnter(ParameterListNode node) {
		
				

	}
	@Override
	public void visitLeave(ParameterListNode node) {
	
		IdentifierNode identifier = null; //(IdentifierNode) node.child(0);
		// bind params
		boolean isImmutable=false;

		//IdentifierNode identifier = null; //(IdentifierNode) node.child(0);
		for(ParseNode paramSpec:node.getChildren()) {
			identifier = (IdentifierNode) paramSpec.child(1);
			identifier.setType(paramSpec.child(0).getType()); //redundant
			addBinding(identifier,paramSpec.child(0).getType(), isImmutable);
		
			
			
		}	

	}
	@Override
	public void visitLeave(RangeNode node) {
		Type subtype = node.child(0).getType();
		Type arrayType = new Array(subtype);
		node.setType(arrayType);

	}
	@Override
	public void visitEnter(IfNode node) {
		enterSubscope(node);

	}
	@Override
	public void visitLeave(IfNode node) {
		leaveScope(node);
		

	}
	
	@Override
	public void visitEnter(WhileNode node) {
		enterSubscope(node);

	}
	@Override
	public void visitLeave(WhileNode node) {
		leaveScope(node);

	}
	
	@Override
	public void visitLeave(ForNodeX node) {
	
		IdentifierNode ident = (IdentifierNode) node.getChildren().get(0);
		Range rangeType = (Range) node.getChildren().get(1).getType();
		ident.setType(rangeType.getSubtype());
		addBinding(ident,rangeType.getSubtype() , true);
		

	}
	@Override
	public void visitEnter(ForNode node) {
		enterSubscope(node);
		

	}
	
	@Override
	public void visitLeave(ForNode node) {
		
		leaveScope(node);

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
	private void addGlobalBinding(IdentifierNode identifierNode, Type type, boolean isImmutable) {
		Scope scope = identifierNode.getGlobalScope();
		Binding binding = scope.createBinding(identifierNode, type,isImmutable);
		identifierNode.setBinding(binding);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing
	private void incorrectNumberOfParametersError(ParseNode node,  Type[] childTypes) {
		Token token = node.getToken();
		
		logError("provided incorrect number of parameters in function " + node.child(0).getToken().getLexeme() + " " 
				 + childTypes  + " at " + token.getLocation());	
	}
	private void parameterExpressionListTypeError(ParseNode node, Type formal, Type actual) {
		Token token = node.getToken();
		
		logError("types did not match in function " + node.child(0).getToken().getLexeme() + " "+formal + "  and " 
				 + actual  + " at " + token.getLocation());	
	}
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
	private void returnNotInFunctionError(ParseNode node) {
		Token token = node.getToken();
		
		logError("return is not in function");	
	}
	private void invalidReturntypeError(ParseNode node, Type funcType, Type returnType) {
		Token token = node.getToken();
		logError("return type  has invalid type " 
				+ funcType  + " and " + returnType + " at " + token.getLocation());
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