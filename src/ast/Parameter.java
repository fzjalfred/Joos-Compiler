package ast;

import java.util.List;

public class Parameter extends ASTNode {
    public Parameter(List<ASTNode> children, String value){
        super(children, value);
    }
}