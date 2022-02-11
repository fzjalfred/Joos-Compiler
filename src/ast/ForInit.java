package ast;

import java.util.List;

public class ForInit extends ASTNode implements Referenceable{
    public ForInit(List<ASTNode> children, String value){
        super(children, value);
    }
    public boolean isVarDecl(){
        return (children.get(1) != null);
    }
    public VarDeclarator getVarDeclarator(){
        if (isVarDecl()){
            assert children.get(1) instanceof VarDeclarator;
            return (VarDeclarator)children.get(1);
        }
        return null;
    }
}