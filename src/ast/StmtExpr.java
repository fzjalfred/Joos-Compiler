package ast;

import java.util.List;

public class StmtExpr extends ASTNode {
    public StmtExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}