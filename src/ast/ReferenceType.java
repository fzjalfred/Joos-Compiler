package ast;

import java.util.List;

public class ReferenceType extends Type {
    public Referenceable typeDecl;
    public ReferenceType(List<ASTNode> children, String value){
        super(children, value);
        typeDecl = null;
    }
}
