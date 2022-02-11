package ast;

import java.util.List;

public class LocalVarDecl extends LocalVarDeclStmt implements Referenceable{
    public LocalVarDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public List<String> getName(){
        return getVarDeclarators().getName();
    }
    public VarDeclarators getVarDeclarators(){
        assert children.get(1) instanceof VarDeclarators;
        return (VarDeclarators)children.get(1);
    }
}
