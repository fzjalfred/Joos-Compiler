package ast;

import java.util.List;

public class MethodBody extends ASTNode {
    public MethodBody(List<ASTNode> children, String value){
        super(children, value);
    }
    public Block getBlock(){
        assert children.get(0) instanceof Block;
        return (Block)children.get(0);
    }
}
