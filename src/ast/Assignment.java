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
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
