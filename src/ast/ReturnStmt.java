package ast;

import java.util.List;

public class ReturnStmt extends StmtWithoutSubstmt {
    public ReturnStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}