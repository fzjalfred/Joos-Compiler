package ast;

import java.util.ArrayList;
import java.util.List;

public class ParameterList extends ASTNode {
    public ParameterList(List<ASTNode> children, String value){
        super(children, value);
    }
    public int paramNum(){
        return children.size();
    }
    public List<Parameter> getParams(){
        List<Parameter> params = new ArrayList<Parameter>();
        for (ASTNode node : children){
            assert node instanceof Parameter;
            params.add((Parameter)node);
        }
        return params;
    }
}
