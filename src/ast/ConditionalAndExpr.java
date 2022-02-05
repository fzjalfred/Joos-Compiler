package ast;

import java.util.List;

public class ConditionalAndExpr extends ConditionalOrExpr {
    public ConditionalAndExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}