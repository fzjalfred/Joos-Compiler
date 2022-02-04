package ast;

import java.util.List;

public class ConditionalAndExpr extends Expr {
    public ConditionalAndExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}