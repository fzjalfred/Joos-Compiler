package ast;

import java.util.List;

public class EqualityExpr extends AndExpr {
    public EqualityExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}