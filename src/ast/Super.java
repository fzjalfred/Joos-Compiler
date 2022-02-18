package ast;

import java.util.List;

public class Super extends ASTNode {
    public Super(List<ASTNode> children, String value){
        super(children, value);
    }
    public Type getType(){
        assert (children.get(0) instanceof Type);
        return (Type)(children.get(0));
    }
}
