package ast;

import visitors.Visitor;

import java.util.List;

public class InterfaceBody extends ASTNode{
    public InterfaceBody(List<ASTNode> children, String value){
        super(children, value);
    }
    public InterfaceMemberDecls getInterfaceMemberDecls(){
        assert children.get(0) instanceof InterfaceMemberDecls;
        return (InterfaceMemberDecls)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
