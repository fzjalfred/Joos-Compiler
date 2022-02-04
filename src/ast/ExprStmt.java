package ast;

import java.util.List;

public class ExprStmt extends ASTNode{
    public ExprStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}