package ast;

import visitors.TypeCheckVisitor;
import visitors.Visitor;

import java.util.List;

public class AbstractMethodDecl extends InterfaceMemberDecl{
    public AbstractMethodDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public MethodDeclarator getMethodDeclarator(){
        assert children.get(2) instanceof MethodDeclarator;
        return (MethodDeclarator)children.get(2);
    }
    private boolean ifContainModifier(ASTNode modifiers, String name){
        if (modifiers == null) return false;
        for (ASTNode n : modifiers.children){
            if (n.value == name) return true;
        }
        return false;
    }
    public boolean isPublic() {
        return ifContainModifier(children.get(0), "public");
    }
    public String getName(){
        return getMethodDeclarator().getName();
    }

    public boolean isStatic() {
        return false;
    }

    public Type getType(){
        if(children.get(1) instanceof Type){
            return (Type)children.get(1);
        }
        return null;
    }

    private void acceptMain(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    @Override
    public void accept(Visitor v){
        if (v instanceof TypeCheckVisitor){
            TypeCheckVisitor visitor = (TypeCheckVisitor)v;
            visitor.context.entry("Abstracr Method Parameter List");
            acceptMain(v);
            visitor.context.pop();
        }   else{
            acceptMain(v);
        }
    }
}
