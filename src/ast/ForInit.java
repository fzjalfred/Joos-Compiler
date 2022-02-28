package ast;

import visitors.Visitor;

import java.util.List;

public class ForInit extends ASTNode implements Referenceable{
    public ForInit(List<ASTNode> children, String value){
        super(children, value);
    }
    public boolean isVarDecl(){
        return (children.get(1) != null);
    }
    public VarDeclarator getVarDeclarator(){
        if (isVarDecl()){
            assert children.get(1) instanceof VarDeclarator;
            return (VarDeclarator)children.get(1);
        }
        return null;
    }

    public Type getType(){
        if (isVarDecl()){
            assert children.get(0) instanceof Type;
            return (Type)children.get(0);
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