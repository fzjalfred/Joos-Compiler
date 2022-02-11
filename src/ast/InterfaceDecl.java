package ast;

import java.util.List;

public class InterfaceDecl extends TypeDecl {
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
}
