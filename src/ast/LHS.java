package ast;

import visitors.Visitor;

import java.util.List;

public class LHS extends Expr {
    public boolean isAssignable = true;

    public LHS(List<ASTNode> children, String value){
        super(children, value);
    }

    public boolean hasName() {
        if (children.size() == 0) {
            return false;
        }
        return (children.get(0) instanceof Name);
    }

    public Name getName() {
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    public Expr getExpr(){
        if (hasName()) return null;
        return (Expr)children.get(0);
    }

    @Override
    public void accept(Visitor v) {
        for (ASTNode node : children) {
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}