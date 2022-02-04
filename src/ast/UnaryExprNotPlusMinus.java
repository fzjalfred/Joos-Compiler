package ast;

import java.util.List;

public class UnaryExprNotPlusMinus extends Expr {
    public UnaryExprNotPlusMinus(List<ASTNode> children, String value){
        super(children, value);
    }
}