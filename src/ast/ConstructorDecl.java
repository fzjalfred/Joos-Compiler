package ast;

import java.util.List;

public class ConstructorDecl extends ClassBodyDecl implements Referenceable{
    public ConstructorDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return getConstructorDeclarator().getName();
    }

    public ConstructorDeclarator getConstructorDeclarator(){
        assert children.get(1) instanceof ConstructorDeclarator;
        return (ConstructorDeclarator)children.get(1);
    }

}
