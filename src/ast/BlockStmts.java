package ast;

import java.util.List;

public class BlockStmts extends ASTNode{
    public BlockStmts(List<ASTNode> children, String value){
        super(children, value);
    }
}