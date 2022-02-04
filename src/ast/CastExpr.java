package ast;

import java.util.List;

public class CastExpr extends Expr {
    public CastExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}