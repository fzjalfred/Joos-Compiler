package ast;

import java.util.List;

public class AdditiveExpr extends Expr {
    public AdditiveExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}