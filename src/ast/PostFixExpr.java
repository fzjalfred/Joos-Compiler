package ast;

import visitors.Visitor;

import java.util.List;

public class PostFixExpr extends UnaryExprNotPlusMinus {
    public Type type = null;
    public PostFixExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    public Type getType() {
        if (type != null) {
            return type;
        }
        if (children.get(0) instanceof Primary) {
            type = ((Primary) children.get(0)).getType();
            return type;
        } else {
            // FIXME
            type = ((Name)children.get(0)).type;
            return type;
        }
    }

    public boolean hasName() {
        if (children.size() == 0) {
            return false;
        }
        return children.get(0) instanceof Name;
    }

    public Name getName(){
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}