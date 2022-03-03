package ast;

import visitors.TypeCheckVisitor;
import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

public class MethodDecl extends ClassMemberDecl implements Callable {
    public MethodDecl(List<ASTNode> children, String value){

        super(children, value);
    }

    public MethodHeader getMethodHeader(){
        assert children.get(0) instanceof MethodHeader;
        return (MethodHeader)children.get(0);
    }

    public MethodBody getMethodBody(){
        assert children.get(1) instanceof MethodBody;
        return (MethodBody)children.get(1);
    }

    public String getName(){
        MethodHeader mh = getMethodHeader();
        return mh.getName();
    }

    public List<Type> getParamType() {
        MethodHeader methodHeader = getMethodHeader();
        MethodDeclarator methodDeclarator = methodHeader.getMethodDeclarator();
        if (methodDeclarator.hasParameterList()){
            return methodDeclarator.getParameterList().getParamType();
        }
        return null;
    }

    public Type getReturnType() {
        Type returnType = (Type)getMethodHeader().getType();
        return returnType;
    }

    @Override
    public Type getType() {
        return getReturnType();
    }

    public boolean isStatic() {
        Modifiers modifiers = (Modifiers) getMethodHeader().children.get(0);

        for (ASTNode modifier : modifiers.children) {
            if (modifier.value.equals( "static")) {
                return true;
            }
        }
        return false;
    }

    private boolean ifContainModifier(ASTNode modifiers, String name){
        if (modifiers == null) return false;
        for (ASTNode n : modifiers.children){
            if (n.value == name) return true;
        }
        return false;
    }

    public boolean isAbstract() {
        return ifContainModifier(children.get(0).children.get(0), "abstract");
    }

    public boolean isPublic() {
        return ifContainModifier(children.get(0).children.get(0), "public") ;
    }
    public boolean isProtected() {
        return ifContainModifier(children.get(0).children.get(0), "protected") ;
    }


    private void acceptMain(Visitor v){
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
    }

    @Override
    public void accept(Visitor v){
        if (v instanceof TypeCheckVisitor){
            TypeCheckVisitor visitor = (TypeCheckVisitor)v;
            visitor.context.entry("Method Parameter List");
            acceptMain(v);
            visitor.context.pop();
        }   else{
            acceptMain(v);
        }
    }
}
