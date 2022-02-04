package ast;

import java.util.List;

public class BlockStmt extends ASTNode{
    public BlockStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}