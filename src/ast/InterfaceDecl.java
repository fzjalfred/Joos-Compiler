package ast;

import utils.tools;
import visitors.Visitor;

import java.util.List;
import java.util.*;

public class InterfaceDecl extends TypeDecl {

    public Map<String, List<ASTNode>> containMap = null;
    public Map<AbstractMethodDecl, Integer> interfaceMethodMap = new HashMap<AbstractMethodDecl, Integer>();
    public Integer itable_offset_counter = 0;

    public InterfaceDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(1).value;
    }
    public InterfaceBody getInterfaceBody(){
        assert children.get(3) instanceof InterfaceBody;
        return (InterfaceBody)children.get(3);
    }

    public boolean hasMemberDecls() {
        if (children.get(3) instanceof InterfaceBody) {
            InterfaceBody body = getInterfaceBody();
            return body.children.get(0) instanceof InterfaceMemberDecls;
        }
        return false;
    }

    public InterfaceMemberDecls getInterfaceMemberDecls() {
        assert children.get(3) instanceof InterfaceBody;
        InterfaceBody body = getInterfaceBody();
        assert body.children.get(0) instanceof InterfaceMemberDecls;
        return (InterfaceMemberDecls)body.children.get(0);
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

    @Override
    public Type getType() {
        return tools.getClassType(getName(), this);
    }
}
