package ast;

import visitors.Visitor;

import java.util.List;

public class RelationExpr extends EqualityExpr {
    Integer val = null;
    public RelationExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    public Type getType(){
        if (children.size() >= 3 && children.get(2) instanceof Type){
            return (Type)children.get(2);
        }
        return null;
    }

    public boolean isInstanceOf(){
        if (children.size() >= 3 && children.get(1) != null){
            return children.get(1).value.equals("instanceof");
        }
        return false;
    }


    public Expr getisInstanceOfLeft() {
        assert isInstanceOf();
        return (Expr)children.get(0);
    }
    public Type getisInstanceOfRight() {
        assert isInstanceOf();
        return (Type)children.get(2);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}