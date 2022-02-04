package ast;

import java.util.List;

public class PostFixExpr extends Expr {
    public PostFixExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}