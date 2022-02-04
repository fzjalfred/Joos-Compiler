package ast;

import java.util.List;

public class ClassInstanceCreateExpr extends Expr {
    public ClassInstanceCreateExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}