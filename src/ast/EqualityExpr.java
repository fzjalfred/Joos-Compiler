package ast;

import java.util.List;

public class EqualityExpr extends Expr {
    public EqualityExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}