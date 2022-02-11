package ast;

import java.util.List;

public class Parameter extends ASTNode implements Referenceable {
    public Parameter(List<ASTNode> children, String value){
        super(children, value);
    }
    public VarDeclaratorID getVarDeclaratorID(){
        assert children.get(1) instanceof VarDeclaratorID;
        return (VarDeclaratorID)children.get(1);
    }
}