package ast;

import java.util.List;

public class ConstructorDeclarator extends ASTNode {
    public ConstructorDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(0).value;
    }
}