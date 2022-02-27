package ast;

import visitors.Visitor;

import java.util.List;

public class Expr extends ASTNode {
    public Expr(List<ASTNode> children, String value){
        super(children, value);
    }
    public Type type;


    public boolean isBinary(){
        return  (children.size() > 1 && children.get(1) != null);
    }

    public ASTNode getLeft(){
        return children.get(0);
    }

    public ASTNode getRight(){
        return children.get(1);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
