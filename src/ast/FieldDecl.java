package ast;

import java.util.List;

public class FieldDecl extends ClassMemberDecl {
    public FieldDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public List<String> getName(){
        VarDeclarators varDeclarators = getVarDeclarators();
        return varDeclarators.getName();
    }

    public VarDeclarators getVarDeclarators(){
        assert children.get(2) instanceof VarDeclarators;
        return (VarDeclarators)children.get(2);
    }
}
