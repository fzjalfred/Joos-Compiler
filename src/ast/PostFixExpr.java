package ast;

import java.util.List;

public class PostFixExpr extends UnaryExprNotPlusMinus {
    public PostFixExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}