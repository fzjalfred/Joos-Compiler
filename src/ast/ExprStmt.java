package ast;

import java.util.List;

public class ExprStmt extends StmtWithoutSubstmt{
    public ExprStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}