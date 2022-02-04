package ast;

import java.util.List;

public class FieldAccess extends Expr {
    public FieldAccess(List<ASTNode> children, String value){
        super(children, value);
    }
}