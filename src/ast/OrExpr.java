package ast;

import java.util.List;

public class OrExpr extends Expr {
    public OrExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}