package ast;

import visitors.Visitor;

import java.util.List;

public class Interfaces extends ASTNode{
    public Interfaces(List<ASTNode> children, String value){
        super(children, value);
    }

    public InterfaceTypeList getInterfaceTypeList(){
        assert children.get(0) instanceof InterfaceTypeList;
        return (InterfaceTypeList)children.get(0);

    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
