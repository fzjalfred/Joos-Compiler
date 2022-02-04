package ast;

import java.util.List;

public class IfThenStmt extends Stmt {
    public IfThenStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}