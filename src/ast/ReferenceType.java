package ast;

import visitors.Visitor;

import java.util.List;
import java.util.Objects;

public class ReferenceType extends Type {
    public Referenceable typeDecl;
    public ReferenceType(List<ASTNode> children, String value){
        super(children, value);
        typeDecl = null;
    }

    public boolean equals(ReferenceType type){
        if (this instanceof  ClassOrInterfaceType && type instanceof ClassOrInterfaceType){
            ClassOrInterfaceType thisType = (ClassOrInterfaceType)this;
            ClassOrInterfaceType thatType = (ClassOrInterfaceType)type;
            return thisType.equals(thatType);
        }   else if (this instanceof ArrayType && type instanceof ArrayType){
            ArrayType thisType = (ArrayType)this;
            ArrayType thatType = (ArrayType)type;
            return thisType.equals(thatType);
        }
        return false;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
