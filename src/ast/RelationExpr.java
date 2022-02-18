package ast;

import java.util.List;

public class RelationExpr extends EqualityExpr {
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
}