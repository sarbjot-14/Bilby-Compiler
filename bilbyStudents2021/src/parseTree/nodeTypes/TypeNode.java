package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

import org.w3c.dom.Node;

import inputHandler.Locator;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import tokens.LextantToken;
import tokens.Token;

public class TypeNode extends ParseNode {

	public TypeNode(Token token) {
		super(token);
		//assert(token.isLextant(Keyword.CAST));
	}

	public TypeNode(ParseNode node) {
		super(node);
	}
	
	
	////////////////////////////////////////////////////////////
	// attributes
	
//	public Lextant getDeclarationType() {
//		return lextantToken().getLextant();
//	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static TypeNode withChildren(Token token, ParseNode type) {
		TypeNode node = new TypeNode(token);
		node.appendChild(type);
		return node;
	}
	
	public static TypeNode make(Token token) {
		return new TypeNode(token);
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
	
	public Token typeToken() {
		return this.token;
		
	}

	public boolean isArray() {
		
		return this.typeToken().isLextant(Punctuator.OPEN_BRACKET);
	}
	public boolean isRange() {

		return this.typeToken().isLextant(Punctuator.LESS);
	}

	public Type typeFromToken() {
		return PrimitiveType.fromToken(this.typeToken());
	}
}
