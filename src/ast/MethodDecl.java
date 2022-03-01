package ast;

import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

public class MethodDecl extends ClassMemberDecl {
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
        if (!methodDeclarator.hasParameterList()) {
            return null;
        }
        ParameterList parameterList = methodDeclarator.getParameterList();
        List<Parameter> parameters = parameterList.getParams();


        List<Type> typeList = new ArrayList<Type>();

        for (Parameter parameter : parameters) {
            typeList.add(parameter.getType());
        }
        return typeList;
    }

    public Type getReturnType() {
        Type returnType = (Type)getMethodHeader().children.get(1);
        return returnType;
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
    public boolean isProtected() {
        return ifContainModifier(children.get(0).children.get(0), "protected");
    }


    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
