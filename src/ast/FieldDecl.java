package ast;

import visitors.IRTranslatorVisitor;
import visitors.TypeCheckVisitor;
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

    public boolean hasRight() {
        VarDeclarators varDeclarators = getVarDeclarators();
        VarDeclarator varDeclarator = (VarDeclarator) varDeclarators.children.get(0);
        return varDeclarator.hasExpr();
    }

    public Expr getExpr() {
        VarDeclarators varDeclarators = getVarDeclarators();
        VarDeclarator varDeclarator = (VarDeclarator) varDeclarators.children.get(0);
        return varDeclarator.getExpr();
    }

    public String getFirstVarName(){
        return getName().get(0);
    }

    public VarDeclarators getVarDeclarators(){
        assert children.get(2) instanceof VarDeclarators;
        return (VarDeclarators)children.get(2);
    }

    @Override
    public Type getType(){
        assert children.get(1) instanceof Type;
        return (Type)children.get(1);
    }

    public boolean isStatic() {
        if (children.size() == 0) return false;
        Modifiers modifiers = (Modifiers) children.get(0);
        if (modifiers == null) return false;
        for (ASTNode modifier : modifiers.children) {
            if (modifier.value.equals("static")) {
                return true;
            }
        }
        return false;
    }

    public Modifiers getModifiers(){
        assert children.get(0) instanceof Modifiers;
        return (Modifiers)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        if (v instanceof TypeCheckVisitor){
            String var = getVarDeclarators().getFirstName();
            ((TypeCheckVisitor)v).context.put(var, this);
        }
    }

    @Override
    public String toString() {
        if (!value.equals("")){
            return value;
        }
        return getFirstVarName();
    }
}
