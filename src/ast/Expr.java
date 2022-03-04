package ast;

import visitors.Visitor;

import java.util.List;

public class Expr extends ASTNode {
    public Expr(List<ASTNode> children, String value){
        super(children, value);
    }
    public Type type;

    public Expr getSingleChild() {
        assert children.size() == 1;
        return (Expr)children.get(0);
    }

    public boolean isBinary(){
        return  (children.size() > 1 && children.get(1) != null);
    }

    public Expr getOperatorLeft() {
        return (Expr)children.get(0);
    }

    public Expr getOperatorRight() {
        if (children.size() == 3){
            return (Expr)children.get(2);
        }   else if (children.size() == 2){
            return (Expr)children.get(1);
        }
        return (Expr)children.get(0);

    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
