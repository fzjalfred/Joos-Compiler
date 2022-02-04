package ast;

import java.util.List;

public class ArgumentList extends ASTNode {
    public ArgumentList(List<ASTNode> children, String value){
        super(children, value);
    }
}