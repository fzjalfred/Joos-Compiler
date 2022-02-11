package ast;

import java.util.List;

public class VarDeclaratorID extends ASTNode{
    public VarDeclaratorID(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        assert !children.get(0).value.equals("");
        return children.get(0).value;
    }
}
