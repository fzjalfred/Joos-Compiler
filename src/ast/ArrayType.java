package ast;

import java.util.List;

public class ArrayType extends ReferenceType{
    public ArrayType(List<ASTNode> children, String value){
        super(children, value);
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
}
