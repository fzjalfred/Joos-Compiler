package ast;

import java.util.List;

public class ReferenceType extends Type {
    public ReferenceType(List<ASTNode> children, String value){
        super(children, value);
    }
}
