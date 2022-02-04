package ast;

import java.util.List;

public class RelationExpr extends Expr {
    public RelationExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}