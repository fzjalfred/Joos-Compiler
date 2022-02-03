package ast;

import java.util.List;

public class ClassOrInterfaceType extends ReferenceType {
    public ClassOrInterfaceType(List<ASTNode> children, String value){
        super(children, value);
    }
}
