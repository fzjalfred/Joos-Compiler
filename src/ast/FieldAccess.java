package ast;

import visitors.Visitor;

import java.util.List;

public class FieldAccess extends PrimaryNoArray {
    public FieldAccess(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}