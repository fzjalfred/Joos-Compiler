package ast;

import java.util.List;

public class CastExpr extends UnaryExprNotPlusMinus {
    public CastExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}