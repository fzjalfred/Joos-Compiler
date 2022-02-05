package ast;

import java.util.List;

public class AdditiveExpr extends RelationExpr {
    public AdditiveExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}