package ast;

import lexer.Token;
import visitors.Visitor;

import java.util.List;

public class PrimaryNoArray extends Primary {

    public PrimaryNoArray(List<ASTNode> children, String value){
        super(children, value);
    }

    public Expr getExpr(){
        return (Expr)children.get(0);
    }

    public Type getType() {
        if (type != null) {
            return type;
        }

        if (children.get(0) instanceof ThisLiteral) {
            return null;
        }
        if (children.get(0) instanceof Expr) {
            if (children.get(0) instanceof Primary) {
                type = ((Primary)children.get(0)).getType();
                return type;
            } else if (children.get(0) instanceof PostFixExpr) {
                type = ((PostFixExpr)children.get(0)).getType();
            }
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