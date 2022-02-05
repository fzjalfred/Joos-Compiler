package ast;

import java.util.List;

public class EmptyStmt extends StmtWithoutSubstmt {
    public EmptyStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}