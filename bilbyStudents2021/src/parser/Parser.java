package parser;

import java.util.Arrays;

import logging.BilbyLogger;
import parseTree.*;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.StatementBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
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
import tokens.*;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import lexicalAnalyzer.Scanner;


public class Parser {
	private Scanner scanner;
	private Token nowReading;
	private Token previouslyRead;
	
	public static ParseNode parse(Scanner scanner) {
		Parser parser = new Parser(scanner);
		return parser.parse();
	}
	public Parser(Scanner scanner) {
		super();
		this.scanner = scanner;
	}
	
	public ParseNode parse() {
		readToken();
		return parseProgram();
	}

	////////////////////////////////////////////////////////////
	// "program" is the start symbol S
	// S -> MAIN StatementBlock
	
	private ParseNode parseProgram() {
		if(!startsProgram(nowReading)) {
			return syntaxErrorNode("program");
		}
		ParseNode program = new ProgramNode(nowReading);
		// terminal? -> expect
		expect(Keyword.MAIN); // current token is equal to one of it's arguments
		ParseNode blockStatement = parseBlockStatement(); // Recursively parse, 
		program.appendChild(blockStatement);
		
		if(!(nowReading instanceof NullToken)) { // end of input
			return syntaxErrorNode("end of program");
		}
		
		return program;
	}
	private boolean startsProgram(Token token) {
		return token.isLextant(Keyword.MAIN);
	}
	
	
	///////////////////////////////////////////////////////////
	// statementBlock
	
	// statementBlock -> { statement* }
		private ParseNode parseBlockStatement() {
			if(!startsBlockStatement(nowReading)) {
				return syntaxErrorNode("statementBlock");
		}
		ParseNode blockStatement = new StatementBlockNode(nowReading);
		expect(Punctuator.OPEN_BRACE);
		
		while(startsStatement(nowReading)) { // 0 or more statements
			ParseNode statement = parseStatement();
			blockStatement.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return blockStatement;
	}
	private boolean startsBlockStatement(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}
	
	
	///////////////////////////////////////////////////////////
	// statements
	
	// statement-> declaration | printStmt | blockStatement
	private ParseNode parseStatement() {
		if(!startsStatement(nowReading)) {
			return syntaxErrorNode("statement");
		}
		if(startsDeclaration(nowReading)) {
			return parseDeclaration();
		}
		if(startsPrintStatement(nowReading)) {
			return parsePrintStatement();
		}
		if(startsBlockStatement(nowReading)) {
			return parseBlockStatement();
		}
		// add if and other things
		return syntaxErrorNode("statement");
	}
	private boolean startsStatement(Token token) {
		return startsPrintStatement(token) ||
				startsDeclaration(token) || startsBlockStatement(token);
	}
	
	// printStmt -> PRINT printExpressionList TERMINATOR
	private ParseNode parsePrintStatement() {
		if(!startsPrintStatement(nowReading)) {
			return syntaxErrorNode("print statement");
		}
		
		ParseNode result = new PrintStatementNode(nowReading);
		
		expect(Keyword.PRINT);
		result = parsePrintExpressionList(result);// pass it parent. Put all of expression list as children of result (parent)
		// result is parent
		expect(Punctuator.TERMINATOR);
		return result;
	}
	private boolean startsPrintStatement(Token token) {
		return token.isLextant(Keyword.PRINT);
	}	

	// This adds the printExpressions it parses to the children of the given parent
	// printExpressionList -> printSeparator* (expression printSeparator+)* expression? (note that this is nullable)

	private ParseNode parsePrintExpressionList(ParseNode parent) {
		if(!startsPrintExpressionList(nowReading)) {
			return syntaxErrorNode("printExpressionList");
		}
		
		while(startsPrintSeparator(nowReading)) {
			parsePrintSeparator(parent);
		}
		while(startsExpression(nowReading)) {
			parent.appendChild(parseExpression());
			if(nowReading.isLextant(Punctuator.TERMINATOR)) {
				return parent;
			}
			do { // do whiles good for + in regex
				parsePrintSeparator(parent);
			} while(startsPrintSeparator(nowReading));
		}
		return parent;
	}	
	private boolean startsPrintExpressionList(Token token) {
		return startsExpression(token) || startsPrintSeparator(token);
	}

	
	// This adds the printSeparator it parses to the children of the given parent
	// printSeparator -> PRINT_SEPARATOR | PRINT_SPACE | PRINT_NEWLINE | PRINT_TAB
	
	private void parsePrintSeparator(ParseNode parent) {
		if(!startsPrintSeparator(nowReading)) {
			ParseNode child = syntaxErrorNode("print separator");
			parent.appendChild(child);
			return;
		}
		
		if(nowReading.isLextant(Punctuator.PRINT_NEWLINE)) { 
			readToken();
			ParseNode child = new NewlineNode(previouslyRead); // prevRead is $n
			parent.appendChild(child);
		}		
		else if(nowReading.isLextant(Punctuator.PRINT_SPACE)) {
			readToken();
			ParseNode child = new SpaceNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Punctuator.PRINT_TAB)) {
			readToken();
			ParseNode child = new TabNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Punctuator.PRINT_SEPARATOR)) {
			readToken();
		} 
	}
	private boolean startsPrintSeparator(Token token) {
		return token.isLextant(Punctuator.PRINT_SEPARATOR, Punctuator.PRINT_SPACE, Punctuator.PRINT_NEWLINE,  Punctuator.PRINT_TAB);
	}
	
	
	// declaration -> IMM identifier := expression TERMINATOR
	private ParseNode parseDeclaration() {
		if(!startsDeclaration(nowReading)) {
			return syntaxErrorNode("declaration");
		}
		Token declarationToken = nowReading;
		readToken();
		
		ParseNode identifier = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode initializer = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return DeclarationNode.withChildren(declarationToken, identifier, initializer);
	}
	private boolean startsDeclaration(Token token) {
		return token.isLextant(Keyword.IMM);
	}


	
	///////////////////////////////////////////////////////////
	// expressions
	// expr                     -> comparisonExpression
	// comparisonExpression     -> additiveExpression [COMPARISON additiveExpression]?
	// additiveExpression       -> multiplicativeExpression [ADDOP multiplicativeExpression]*  (left-assoc)
	// multiplicativeExpression -> unaryExpression [MULTOP unaryExpression]*  (left-assoc)
	// unaryExpression			-> UNARYOP* unaryExpression | atomicExpression
	// atomicExpression         -> bracketExpression | literal
	// bracketExpression       -> (expression) | [ expression CAST type ]
	// literal                  -> intConstant | identifier | booleanConstant | characterConstant | stringConstant | floatConstant 

	// expr  -> comparisonExpression
	private ParseNode parseExpression() {		
		if(!startsExpression(nowReading)) {
			return syntaxErrorNode("expression");
		}
		return parseComparisonExpression();
	}
	private boolean startsExpression(Token token) {
		return startsComparisonExpression(token);
	}

	// comparisonExpression     -> additiveExpression [COMPARISON additiveExpression]?
	private ParseNode parseComparisonExpression() {
		if(!startsComparisonExpression(nowReading)) {
			return syntaxErrorNode("comparison expression");
		}
		
		ParseNode left = parseAdditiveExpression();
		if(nowReading.isLextant(Punctuator.GREATER, Punctuator.GREATER_THAN_EQUAL,Punctuator.LESS,Punctuator.LESS_THAN_EQUAL, Punctuator.EQUALS,Punctuator.NOT_EQUALS  )) {
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseAdditiveExpression();
			
			return OperatorNode.withChildren(compareToken, left, right);
		}
		
		return left;

	}
	private boolean startsComparisonExpression(Token token) {
		return startsAdditiveExpression(token);
	}

	// additiveExpression -> multiplicativeExpression [ADDOP multiplicativeExpression]*  (left-assoc)
	private ParseNode parseAdditiveExpression() {
		if(!startsAdditiveExpression(nowReading)) {
			return syntaxErrorNode("additiveExpression");
		}
		
		ParseNode left = parseMultiplicativeExpression();
		while(nowReading.isLextant(Punctuator.ADD, Punctuator.SUBTRACT)) { // a + b + c
			Token additiveToken = nowReading;  // a is left, + is parent, b is right, this tree is new left, c is right, + is partent. 
			readToken();
			ParseNode right = parseMultiplicativeExpression();
			
			left = OperatorNode.withChildren(additiveToken, left, right); // left associative
		}
		return left;
	}
	private boolean startsAdditiveExpression(Token token) {
		return startsMultiplicativeExpression(token);
	}	

	// multiplicativeExpression -> unaryExpression [MULTOP unaryExpression]*  (left-assoc)
	private ParseNode parseMultiplicativeExpression() {
		if(!startsMultiplicativeExpression(nowReading)) {
			return syntaxErrorNode("multiplicativeExpression");
		}
		
		ParseNode left = parseUnaryExpression();
		while(nowReading.isLextant(Punctuator.MULTIPLY, Punctuator.DIVIDE)) {
			Token multiplicativeToken = nowReading;
			readToken();
			ParseNode right = parseUnaryExpression();
			
			left = OperatorNode.withChildren(multiplicativeToken, left, right);
		}
		return left;
	}
	private boolean startsMultiplicativeExpression(Token token) {
		return startsUnaryExpression(token);
	}
	
	
	
	// atomicExpression         -> bracketExpression | literal
	private ParseNode parseAtomicExpression() {
		if(!startsAtomicExpression(nowReading)) {
			return syntaxErrorNode("atomic expression");
		}
		if(startsBracketExpression(nowReading)) {
			return parseBracketExpression();
		}
		return parseLiteral();
	}
	private boolean startsAtomicExpression(Token token) {
		return startsLiteral(token) || startsBracketExpression(token);
	}
	
	// bracketExpression       -> (expression) | [ expression CAST type ]
	
	private ParseNode parseBracketExpression() {
		if(!startsBracketExpression(nowReading)) {
			return syntaxErrorNode("bracket expression");
		}
		if(nowReading.isLextant(Punctuator.OPEN_BRACE_PAREN)) {
			expect(Punctuator.OPEN_BRACE_PAREN);
			ParseNode left = parseExpression();
			expect(Punctuator.CLOSE_BRACE_PAREN);
			return left;
		}
		else {
			expect(Punctuator.OPEN_BRACKET);
			
			ParseNode left = parseExpression();
			
			readToken();
			ParseNode castNode = new CastNode(previouslyRead);
			 
			readToken();
			ParseNode rightType = new TypeNode(previouslyRead);
			
			
			castNode.appendChild(left);
			castNode.appendChild(rightType);
			
		
			expect(Punctuator.CLOSE_BRACKET);
			return castNode;
		}
		
	
	}
	
	private boolean startsBracketExpression(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE_PAREN) || token.isLextant(Punctuator.OPEN_BRACKET);
	}
	

	// unaryExpression			-> UNARYOP* unaryExpression | atomicExpression
	private ParseNode parseUnaryExpression() {

		if(!startsUnaryExpression(nowReading) ) {
			return syntaxErrorNode("unary or atomic expression");
		}

        if(nowReading.isLextant(Punctuator.SUBTRACT, Punctuator.ADD))  {
        	
			Token operatorToken = nowReading;
			readToken();
			ParseNode child = parseUnaryExpression();
			return OperatorNode.withChildren(operatorToken, child);
        }
        else {
        	return parseAtomicExpression();
        }
        		
	}
	private boolean startsUnaryExpression(Token token) {
		return (token.isLextant(Punctuator.SUBTRACT,Punctuator.ADD) || startsAtomicExpression(nowReading));
	}
	
	// literal   -> intConstant | identifier | booleanConstant | characterConstant | stringConstant | floatConstant
	private ParseNode parseLiteral() {
		if(!startsLiteral(nowReading)) {
			return syntaxErrorNode("literal");
		}
		
		if(startsIntLiteral(nowReading)) {
			return parseIntLiteral();
		}
		if(startsCharacterLiteral(nowReading)) {
			return parseCharacterLiteral();
		}
		if(startsStringLiteral(nowReading)) {
			return parseStringLiteral();
		}
		if(startsFloatLiteral(nowReading)) {
			return parseFloatLiteral();
		}
		if(startsIdentifier(nowReading)) {
			return parseIdentifier();
		}
		if(startsBooleanLiteral(nowReading)) {
			return parseBooleanLiteral();
		}

		return syntaxErrorNode("literal");
	}
	private boolean startsLiteral(Token token) {
		return startsIntLiteral(token) || startsCharacterLiteral(token) || startsStringLiteral(token) || startsFloatLiteral(token) || startsIdentifier(token) || startsBooleanLiteral(token);
	}

	// integer (literal)
	private ParseNode parseIntLiteral() {
		if(!startsIntLiteral(nowReading)) {
			return syntaxErrorNode("integer constant");
		}
		readToken();
		return new IntegerConstantNode(previouslyRead);
	}
	private boolean startsIntLiteral(Token token) {
		return token instanceof IntegerConstantToken;
	}
	
	//  character (literal)
	private ParseNode parseCharacterLiteral() {
		if(!startsCharacterLiteral(nowReading)) {
			return syntaxErrorNode("character constant");
		}
		readToken();
		return new CharacterConstantNode(previouslyRead);
	}
	private boolean startsCharacterLiteral(Token token) {
		return token instanceof CharacterConstantToken;
	}

	//  string (literal)
	private ParseNode parseStringLiteral() {
		if(!startsStringLiteral(nowReading)) {
			return syntaxErrorNode("string constant");
		}
		readToken();
		return new StringConstantNode(previouslyRead);
	}
	private boolean startsStringLiteral(Token token) {
		return token instanceof StringConstantToken;
	}

	// floating (literal)
		private ParseNode parseFloatLiteral() {
			if(!startsFloatLiteral(nowReading)) {
				return syntaxErrorNode("floating constant");
			}
			readToken();
			return new FloatingConstantNode(previouslyRead);
		}
		private boolean startsFloatLiteral(Token token) {
			return token instanceof FloatingConstantToken;
		}

	// identifier (terminal)
	private ParseNode parseIdentifier() {
		if(!startsIdentifier(nowReading)) {
			return syntaxErrorNode("identifier");
		}
		readToken();
		return new IdentifierNode(previouslyRead);
	}
	private boolean startsIdentifier(Token token) {
		return token instanceof IdentifierToken;
	}

	// boolean literal
	private ParseNode parseBooleanLiteral() {
		if(!startsBooleanLiteral(nowReading)) {
			return syntaxErrorNode("boolean constant");
		}
		readToken();
		return new BooleanConstantNode(previouslyRead);
	}
	private boolean startsBooleanLiteral(Token token) {
		return token.isLextant(Keyword.TRUE, Keyword.FALSE);
	}

	private void readToken() {
		previouslyRead = nowReading;
		nowReading = scanner.next();
	}	
	
	// if the current token is one of the given lextants, read the next token.
	// otherwise, give a syntax error and read next token (to avoid endless looping).
	private void expect(Lextant ...lextants ) {
		if(!nowReading.isLextant(lextants)) {
			syntaxError(nowReading, "expecting " + Arrays.toString(lextants));
		}
		readToken();
	}	
	private ErrorNode syntaxErrorNode(String expectedSymbol) {
		syntaxError(nowReading, "expecting " + expectedSymbol);
		ErrorNode errorNode = new ErrorNode(nowReading);
		readToken();
		return errorNode;
	}
	private void syntaxError(Token token, String errorDescription) {
		String message = "" + token.getLocation() + " " + errorDescription;
		error(message);
	}
	private void error(String message) {
		BilbyLogger log = BilbyLogger.getLogger("compiler.Parser");
		log.severe("syntax error: " + message);
	}	
}

