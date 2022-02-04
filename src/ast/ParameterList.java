package ast;

import java.util.List;

public class ParameterList extends ASTNode {
    public ParameterList(List<ASTNode> children, String value){
        super(children, value);
    }
}
