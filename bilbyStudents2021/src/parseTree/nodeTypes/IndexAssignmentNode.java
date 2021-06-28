package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class IndexAssignmentNode extends ParseNode {

	public IndexAssignmentNode(Token token) {
		super(token);
		assert(token.getLexeme() ==":=" );
	}

	public IndexAssignmentNode(ParseNode node) {
		super(node);
	}


	////////////////////////////////////////////////////////////
	// attributes

	public Lextant getAssignmentType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	


	////////////////////////////////////////////////////////////
	// convenience factory

	public static IndexAssignmentNode withChildren(Token token, ParseNode declaredName,ParseNode index, ParseNode initializer) {
		IndexAssignmentNode node = new IndexAssignmentNode(token);
		node.appendChild(declaredName);
		node.appendChild(index);
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
} 