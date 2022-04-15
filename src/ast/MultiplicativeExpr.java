package ast;

import visitors.Visitor;

import java.util.List;

public class MultiplicativeExpr extends AdditiveExpr {
    public MultiplicativeExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    public String getOperator() {
        if (children.size() == 3){
            return children.get(1).value;
        }
        return "";
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}