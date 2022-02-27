package ast;

import visitors.Visitor;

import java.util.List;

public class Type extends ASTNode {
    public Type(List<ASTNode> children, String value){
        super(children, value);
    }
    public boolean equals(Type type){
        if (this instanceof  PrimitiveType && type instanceof PrimitiveType){
            PrimitiveType thisType = (PrimitiveType)this;
            PrimitiveType thatType = (PrimitiveType)type;
            return thisType.equals(thatType);
        }   else if (this instanceof ReferenceType && type instanceof ReferenceType){
            ReferenceType thisType = (ReferenceType)this;
            ReferenceType thatType = (ReferenceType)type;
            return thisType.equals(thatType);
        }
        return false;
    }

    public String getNameString(){
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
