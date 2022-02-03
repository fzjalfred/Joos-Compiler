package ast;

import java.util.List;

public class Name extends ASTNode{
    public Name(List<ASTNode> children, String value){
        super(children, value);
    }
}
