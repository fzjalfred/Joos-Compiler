package ast;

import java.util.List;

public class AssignmentExpr extends Expr {
    public AssignmentExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}
