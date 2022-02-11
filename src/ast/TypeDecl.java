package ast;

import java.util.List;

public class TypeDecl extends ASTNode implements Referenceable{
    public TypeDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
