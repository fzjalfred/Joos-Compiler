package ast;

import java.util.List;

public class UnaryExpr extends MultiplicativeExpr {
    public UnaryExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}