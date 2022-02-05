package ast;

import java.util.List;

public class AbstractMethodDecl extends InterfaceMemberDecl {
    public AbstractMethodDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
