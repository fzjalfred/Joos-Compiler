package ast;

import visitors.Visitor;

import java.util.List;

public class UnaryExpr extends MultiplicativeExpr {
    public UnaryExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    public UnaryExpr getUnaryExpr() {
        assert children.size() == 2 && children.get(1) instanceof UnaryExpr;
        return (UnaryExpr)children.get(1);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}