package ast;

import java.util.List;

public class ID extends ASTNode {
    public ID(List<ASTNode> children, String value){
        super(children, value);
    }
}
