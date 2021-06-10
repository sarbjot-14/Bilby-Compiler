package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;

import org.w3c.dom.Node;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
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
	
	public static TypeNode withChildren(Token token, ParseNode declaredName, ParseNode initializer) {
		TypeNode node = new TypeNode(token);
		node.appendChild(declaredName);
		node.appendChild(initializer);
		return node;
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
}
