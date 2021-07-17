package parseTree;

import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.BreakNode;
import parseTree.nodeTypes.CastNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ContinueNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.ForNode;
import parseTree.nodeTypes.ForNodeX;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfNode;
import parseTree.nodeTypes.IndexAssignmentNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.RangeNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StatementBlockNode;
import parseTree.nodeTypes.ArrayNode;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {
	
	// non-leaf nodes: visitEnter and visitLeave
	void visitEnter(OperatorNode node);
	void visitLeave(OperatorNode node);
	
	void visitEnter(StatementBlockNode node);
	void visitLeave(StatementBlockNode node);

	void visitEnter(DeclarationNode node);
	void visitLeave(DeclarationNode node);
	
	void visitEnter(AssignmentNode node);
	void visitLeave(AssignmentNode node);

	
	void visitEnter(ParseNode node);
	void visitLeave(ParseNode node);
	
	void visitEnter(PrintStatementNode node);
	void visitLeave(PrintStatementNode node);
	
	void visitEnter(ProgramNode node);
	void visitLeave(ProgramNode node);
	
	void visitEnter(TypeNode node);
	void visitLeave(TypeNode node);
	
	void visitEnter(CastNode node);
	void visitLeave(CastNode node);
	
	void visitEnter(IfNode node);
	void visitLeave(IfNode node);
	
	void visitEnter(WhileNode node);
	void visitLeave(WhileNode node);
	
	void visitEnter(ForNodeX node);
	void visitLeave(ForNodeX node);
	
	void visitEnter(ForNode node);
	void visitLeave(ForNode node);
	
	void visitEnter(ArrayNode node);
	void visitLeave(ArrayNode node);
	
	void visitEnter(RangeNode node);
	void visitLeave(RangeNode node);
	
	void visitEnter(IndexAssignmentNode node);
	void visitLeave(IndexAssignmentNode node);
	
	


	// leaf nodes: visitLeaf only
	void visit(BooleanConstantNode node);
	void visit(ErrorNode node);
	void visit(IdentifierNode node);
	void visit(IntegerConstantNode node);
	void visit(CharacterConstantNode node);
	void visit(StringConstantNode node);
	void visit(FloatingConstantNode node);
	void visit(NewlineNode node);
	void visit(SpaceNode node);
	void visit(TabNode node);
	void visit(CastNode node);
	void visit(ArrayNode node);
	void visit(RangeNode node);
	void visit(BreakNode breakNode);
	void visit(ContinueNode breakNode);

	
	public static class Default implements ParseNodeVisitor
	{
		public void defaultVisit(ParseNode node) {	}
		public void defaultVisitEnter(ParseNode node) {
			defaultVisit(node);
		}
		public void defaultVisitLeave(ParseNode node) {
			defaultVisit(node);
		}		
		public void defaultVisitForLeaf(ParseNode node) {
			defaultVisit(node);
		}
		
		public void visitEnter(OperatorNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(OperatorNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(DeclarationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(DeclarationNode node) {
			defaultVisitLeave(node);
		}	
		
		public void visitEnter(AssignmentNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(AssignmentNode node) {
			defaultVisitLeave(node);
		}	
		public void visitEnter(StatementBlockNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(StatementBlockNode node) {
			defaultVisitLeave(node);
		}				
		public void visitEnter(ParseNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParseNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(PrintStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(PrintStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ProgramNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ProgramNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(CastNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(CastNode node) {
			defaultVisitLeave(node);
		}
		
		public void visitEnter(IndexAssignmentNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(IndexAssignmentNode node) {
			defaultVisitLeave(node);
		}
		
		
		
		
		public void visitEnter(TypeNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(TypeNode node) {
			defaultVisitLeave(node);
		}
		
		public void visitEnter(IfNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(IfNode node) {
			defaultVisitLeave(node);
		}
		
		public void visitEnter(WhileNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(WhileNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ForNodeX node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ForNodeX node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ForNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ForNode node) {
			defaultVisitLeave(node);
		}
		
		public void visitEnter(ArrayNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ArrayNode node) {
			defaultVisitLeave(node);
		}
		
		public void visitEnter(RangeNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(RangeNode node) {
			defaultVisitLeave(node);
		}
		

		public void visit(BooleanConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ErrorNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IdentifierNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IntegerConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(CharacterConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(StringConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(FloatingConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(NewlineNode node) {
			defaultVisitForLeaf(node);
		}	
		public void visit(SpaceNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(TabNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(CastNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IfNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(WhileNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ArrayNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(RangeNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IndexAssignmentNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(BreakNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ContinueNode node) {
			defaultVisitForLeaf(node);
		}
		
	
	}


	



	
	


	
}
