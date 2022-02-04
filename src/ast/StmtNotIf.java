package ast;

import java.util.List;

public class StmtNotIf extends ASTNode {
    public StmtNotIf(List<ASTNode> children, String value){
        super(children, value);
    }
}