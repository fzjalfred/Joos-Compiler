package ast;

import java.util.List;

public class Block extends StmtWithoutSubstmt {
    public Block(List<ASTNode> children, String value){
        super(children, value);
    }
    public BlockStmts getBlockStmts(){
        assert children.get(0) instanceof BlockStmts;
        return (BlockStmts)children.get(0);
    }
}