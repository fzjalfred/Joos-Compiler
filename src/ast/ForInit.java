package ast;

import java.util.List;

public class ForInit extends ASTNode {
    public ForInit(List<ASTNode> children, String value){
        super(children, value);
    }
}