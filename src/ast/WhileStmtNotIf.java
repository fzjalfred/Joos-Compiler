package ast;

import java.util.List;

public class WhileStmtNotIf extends StmtNotIf {
    public WhileStmtNotIf(List<ASTNode> children, String value){
        super(children, value);
    }
}