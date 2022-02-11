package ast;

import java.util.List;

public class InterfaceBody extends ASTNode{
    public InterfaceBody(List<ASTNode> children, String value){
        super(children, value);
    }
    public InterfaceMemberDecls getInterfaceMemberDecls(){
        assert children.get(0) instanceof InterfaceMemberDecls;
        return (InterfaceMemberDecls)children.get(0);
    }
}
