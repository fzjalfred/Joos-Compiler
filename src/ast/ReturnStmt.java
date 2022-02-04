package ast;

import java.util.List;

public class ReturnStmt extends Stmt {
    public ReturnStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}