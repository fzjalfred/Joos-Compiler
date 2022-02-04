package ast;

import java.util.List;

public class IfThenElseStmtNotIf extends StmtNotIf {
    public IfThenElseStmtNotIf(List<ASTNode> children, String value){
        super(children, value);
    }
}