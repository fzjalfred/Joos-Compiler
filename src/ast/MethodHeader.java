package ast;

import visitors.Visitor;

import java.util.List;

public class MethodHeader extends ASTNode {
    public MethodHeader(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        MethodDeclarator md = getMethodDeclarator();
        return md.getName();
    }
    public MethodDeclarator getMethodDeclarator(){
        assert children.get(2) instanceof MethodDeclarator;
        return (MethodDeclarator)children.get(2);
    }

    public Type getType(){
        if(children.get(1) instanceof Type){
            return (Type)children.get(1);
        }
        return null;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
