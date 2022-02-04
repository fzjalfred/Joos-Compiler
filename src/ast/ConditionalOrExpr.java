package ast;

import java.util.List;

public class ConditionalOrExpr extends Expr {
    public ConditionalOrExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}