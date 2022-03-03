package ast;

import visitors.Visitor;

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

    public ConstructorBody getConstructorBody(){
        assert children.get(2) instanceof ConstructorBody;
        return (ConstructorBody)children.get(2);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    @Override
    public Type getType() {
        return null;
    }
}
