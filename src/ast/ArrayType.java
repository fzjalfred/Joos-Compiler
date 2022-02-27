package ast;

import visitors.Visitor;

import java.util.List;

public class ArrayType extends ReferenceType{
    public ArrayType(List<ASTNode> children, String value){
        super(children, value);
    }


    public Type getType(){
        assert children.get(0) instanceof Type;
        return (Type)children.get(0);
    }

    public boolean equals(ArrayType arrayType) {
        if (children.get(0) instanceof PrimitiveType && arrayType.children.get(0) instanceof PrimitiveType){
            PrimitiveType type1 = (PrimitiveType)children.get(0);
            PrimitiveType type2 = (PrimitiveType)arrayType.children.get(0);
            return type1.equals(type2);
        }   else if (children.get(0) instanceof ClassOrInterfaceType && arrayType.children.get(0) instanceof ClassOrInterfaceType){
            ClassOrInterfaceType type1 = (ClassOrInterfaceType)children.get(0);
            ClassOrInterfaceType type2 = (ClassOrInterfaceType)arrayType.children.get(0);
            return type1.equals(type2);
        }
        return false;
    }

    @Override
    public String getNameString() {
        return getType().getNameString() + "[]";
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
