package ast;

import java.util.List;

public class ForUpdate extends ASTNode{
    public ForUpdate(List<ASTNode> children, String value){
        super(children, value);
    }
}