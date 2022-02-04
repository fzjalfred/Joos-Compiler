package ast;

import java.util.List;

public class StmtExprList extends ASTNode {
    public StmtExprList(List<ASTNode> children, String value){
        super(children, value);
    }
}