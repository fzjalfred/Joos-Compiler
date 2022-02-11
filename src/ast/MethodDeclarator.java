package ast;

import java.util.List;

public class MethodDeclarator extends ASTNode {
    public MethodDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(0).value;
    }
    public ParameterList getParameterList(){
        assert children.get(1) instanceof ParameterList;
        return (ParameterList)children.get(1);
    }
}