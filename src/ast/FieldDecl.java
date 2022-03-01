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

    public boolean isStatic() {
        Modifiers modifiers = (Modifiers) children.get(0);

        for (ASTNode modifier : modifiers.children) {
            if (modifier.value.equals( "static")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
