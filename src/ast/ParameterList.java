package ast;

import visitors.TypeCheckVisitor;
import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

public class ParameterList extends ASTNode {
    private List<Type> types;
    private List<Parameter> parameters;
    public ParameterList(List<ASTNode> children, String value){
        super(children, value);
        types = null;
        parameters = null;
    }
    public int paramNum(){
        return children.size();
    }
    public List<Parameter> getParams(){
        if (parameters != null) return parameters;
        parameters = new ArrayList<Parameter>();
        for (ASTNode node : children){
            assert node instanceof Parameter;
            parameters.add((Parameter)node);
        }
        return parameters;
    }

    public List<Type> getParamType(){
        if (types != null) return types;
        types = new ArrayList<Type>();
        for (ASTNode node : children){
            assert node instanceof Parameter;
            types.add(((Parameter)node).getType());
        }
        return types;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
