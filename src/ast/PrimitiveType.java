package ast;

import visitors.Visitor;

import java.util.List;

public class PrimitiveType extends Type {
    public PrimitiveType(List<ASTNode> children, String value){
        super(children, value);
    }


    public boolean equals(PrimitiveType type) {
        return value.equals(type.value);
    }

    @Override
    public String getNameString() {
        return value;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
