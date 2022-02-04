package ast;

import java.util.List;

public class VarDeclarator extends ASTNode{
    public VarDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
}
