package ast;

import visitors.Visitor;

import java.util.List;

public class Primary extends PostFixExpr {
    public Type type = null;
    public Primary(List<ASTNode> children, String value){
        super(children, value);
    }

    public Type getType() {
        if (type != null) {
            return type;
        }
        if (children.get(0) instanceof ArrayCreationExpr || children.get(0) instanceof Literal) {
            return null;
        }
        type = ((PrimaryNoArray)children.get(0)).getType();
        return type;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}