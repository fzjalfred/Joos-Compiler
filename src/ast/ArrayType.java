package ast;

import java.util.List;

public class ArrayType extends ReferenceType{
    public ArrayType(List<ASTNode> children, String value){
        super(children, value);
    }
}
