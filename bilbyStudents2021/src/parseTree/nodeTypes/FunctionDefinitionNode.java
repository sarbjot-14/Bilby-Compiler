package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class FunctionDefinitionNode extends ParseNode {
	private String startExistHandShakeLabel;
	public FunctionDefinitionNode(Token token) {
		super(token);
	}
	public FunctionDefinitionNode(ParseNode node) {
		super(node);
	}
	
	////////////////////////////////////////////////////////////
	// no attributes
	
	public void setStartExistHandShakeLabel(String startExistHandShakeLabel) {
		this.startExistHandShakeLabel = startExistHandShakeLabel;
	}
	
	public String getStartExistHandShakeLabel() {
		return startExistHandShakeLabel;
	}

	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
	
}
