package ast;

import java.util.List;

public class PostFixExpr extends UnaryExprNotPlusMinus {
    public PostFixExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    public Name getName(){
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }
}