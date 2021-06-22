package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class ArrayNode extends ParseNode {
	private FunctionSignature signature;
	
	public ArrayNode(Token token) {
		super(token);
		assert(token instanceof LextantToken);
	}

	public ArrayNode(ParseNode node) {
		super(node);
	}
	
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public Lextant getOperator() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}
	
	public FunctionSignature getSignature() {
		return signature;
	}

	public void setSignature(FunctionSignature signature) {
		this.signature = signature;
	}

	
	////////////////////////////////////////////////////////////
	// convenience factory

	public static ParseNode withChildren(Token token, ParseNode ...children) {
		ArrayNode node = new ArrayNode(token);
		for(ParseNode child: children) {
			node.appendChild(child);
		}
		return node;
	}
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}

