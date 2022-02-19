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
    private boolean ifContainModifier(ASTNode modifiers, String name){
        if (modifiers == null) return false;
        for (ASTNode n : modifiers.children){
            if (n.value == name) return true;
        }
        return false;
    }
    public boolean isPublic() {
        return ifContainModifier(children.get(0), "public");
    }
    public String getName(){
        return getMethodDeclarator().getName();
    }
}
