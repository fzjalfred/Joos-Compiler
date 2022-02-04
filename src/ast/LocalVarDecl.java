package ast;

import java.util.List;

public class LocalVarDecl extends ASTNode {
    public LocalVarDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
