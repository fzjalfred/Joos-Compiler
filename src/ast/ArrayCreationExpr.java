package ast;

import visitors.Visitor;

import java.util.List;

public class ArrayCreationExpr extends Primary {
    public ArrayCreationExpr(List<ASTNode> children, String value){
        super(children, value);
    }
    public Type getType(){
        assert children.get(0) instanceof Type;
        return (Type)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}