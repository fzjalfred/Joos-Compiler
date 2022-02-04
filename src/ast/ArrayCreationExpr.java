package ast;

import java.util.List;

public class ArrayCreationExpr extends Expr {
    public ArrayCreationExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}