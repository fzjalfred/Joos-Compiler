package ast;

import java.util.List;

public class PackageDecl extends ASTNode {
    public PackageDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public Name getName(){
        assert !children.isEmpty();
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }
}
