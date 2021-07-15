package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class WhileNode extends ParseNode {
	private String breakLabel;
	private String continueLabel;
	private FunctionSignature signature;
	
	public WhileNode(Token token) {
		super(token);
		assert(token instanceof LextantToken);
	}

	public WhileNode(ParseNode node) {
		super(node);
	}
	
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public void setBreakLabel(String breakLabel) {
		this.breakLabel = breakLabel;
	}
	public String getBreakLabel() {
		return breakLabel;
	}
	public void setContinueLabel(String continueLabel) {
		this.continueLabel = continueLabel;
	}
	public String getContinueLabel() {
		return continueLabel;
	}
	
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
		WhileNode node = new WhileNode(token);
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

