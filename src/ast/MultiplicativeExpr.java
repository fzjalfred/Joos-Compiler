package ast;

import java.util.List;

public class MultiplicativeExpr extends Expr {
    public MultiplicativeExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}