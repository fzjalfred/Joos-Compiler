package ast;

import java.util.List;

public class Interfaces extends ASTNode{
    public Interfaces(List<ASTNode> children, String value){
        super(children, value);
    }

    public InterfaceTypeList getInterfaceTypeList(){
        assert children.get(0) instanceof InterfaceTypeList;
        return (InterfaceTypeList)children.get(0);

    }
}
