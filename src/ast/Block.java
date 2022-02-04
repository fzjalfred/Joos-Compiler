package ast;

import java.util.List;

public class Block extends ASTNode {
    public Block(List<ASTNode> children, String value){
        super(children, value);
    }
}