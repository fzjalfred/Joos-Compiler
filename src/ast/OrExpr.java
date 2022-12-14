package ast;

import visitors.Visitor;

import java.util.List;

public class OrExpr extends ConditionalAndExpr{
    public OrExpr(List<ASTNode> children, String value) {
        super(children, value);
    }

    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
