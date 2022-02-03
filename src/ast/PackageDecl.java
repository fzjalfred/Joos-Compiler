package ast;

import java.util.List;

public class PackageDecl extends ASTNode {
    public PackageDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
