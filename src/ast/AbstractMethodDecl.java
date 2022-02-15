package ast;

import java.util.List;

public class AbstractMethodDecl extends InterfaceMemberDecl{
    public AbstractMethodDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public MethodDeclarator getMethodDeclarator(){
        assert children.get(2) instanceof MethodDeclarator;
        return (MethodDeclarator)children.get(2);
    }
    public String getName(){
        return getMethodDeclarator().getName();
    }
}
