package ast;

import java.util.List;

public class VarDeclarator extends ASTNode{
    public VarDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        VarDeclaratorID vid = getVarDeclaratorID();
        return vid.getName();
    }
    public VarDeclaratorID getVarDeclaratorID(){
        assert children.get(0) instanceof VarDeclaratorID;
        return (VarDeclaratorID)children.get(0);
    }
}
