package ast;

import java.util.List;

public class Block extends StmtWithoutSubstmt {
    public Block(List<ASTNode> children, String value){
        super(children, value);
    }
}