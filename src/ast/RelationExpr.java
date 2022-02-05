package ast;

import java.util.List;

public class RelationExpr extends EqualityExpr {
    public RelationExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}