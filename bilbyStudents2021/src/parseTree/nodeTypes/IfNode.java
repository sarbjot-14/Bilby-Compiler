package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class IfNode extends ParseNode {
	private FunctionSignature signature;
	
	public IfNode(Token token) {
		super(token);
		assert(token instanceof LextantToken);
	}

	public IfNode(ParseNode node) {
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
		IfNode node = new IfNode(token);
		for(ParseNode child: children) {
			node.appendChild(child);
		}
		return node;
	}
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}

