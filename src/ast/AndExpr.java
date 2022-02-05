package ast;

import java.util.List;

public class AndExpr extends OrExpr {
    public AndExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}