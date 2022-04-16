package ast;

import visitors.Visitor;

import java.util.List;

public abstract class TypeDecl extends ASTNode implements Referenceable{
    public TypeDecl(List<ASTNode> children, String value){
        super(children, value);
    }


    public String getName(){
        if (this instanceof ClassDecl){
            return ((ClassDecl)this).getName();
        }   else {
            return ((InterfaceDecl)this).getName();
        }
    }
    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    @Override
    public abstract Type getType();
}
