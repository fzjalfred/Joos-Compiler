package ast;

import visitors.Visitor;

import java.util.List;

import ast.LHS;

public class Assignment extends AssignmentExpr {
    public Assignment(List<ASTNode> children, String value){
        super(children, value);
    }

    public LHS getAssignmentLeft() {
        return (LHS)children.get(0);
    }

    public Expr getAssignmentRight() {
        return (Expr)children.get(1);
    }


    @Override
    public void accept(Visitor v){
        if (getAssignmentRight() instanceof ClassInstanceCreateExpr){
            ClassInstanceCreateExpr classInstanceCreateExpr = (ClassInstanceCreateExpr)getAssignmentRight();
            classInstanceCreateExpr.receiver = getAssignmentLeft();
        }
        for (int i = children.size()-1; i >= 0; i--){
	    ASTNode node = children.get(i);
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
