package ast;

import java.util.List;

public class StmtWithoutSubstmt extends Stmt {
    public StmtWithoutSubstmt(List<ASTNode> children, String value){
        super(children, value);
    }
}