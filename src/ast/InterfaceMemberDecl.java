package ast;

import java.util.List;

public class InterfaceMemberDecl extends ASTNode implements Referenceable{
    public InterfaceMemberDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
