package ast;

import java.util.List;

public class IfThenElseStmt extends Stmt {
    public IfThenElseStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}