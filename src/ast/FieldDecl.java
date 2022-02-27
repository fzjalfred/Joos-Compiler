package ast;

import visitors.Visitor;

import java.util.List;

public class FieldDecl extends ClassMemberDecl {
    public FieldDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public List<String> getName(){
        VarDeclarators varDeclarators = getVarDeclarators();
        return varDeclarators.getName();
    }

    public String getFirstVarName(){
        return getName().get(0);
    }

    public VarDeclarators getVarDeclarators(){
        assert children.get(2) instanceof VarDeclarators;
        return (VarDeclarators)children.get(2);
    }

    public Type getType(){
        assert children.get(1) instanceof Type;
        return (Type)children.get(1);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
