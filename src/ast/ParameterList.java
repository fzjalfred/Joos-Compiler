package ast;

import visitors.TypeCheckVisitor;
import visitors.Visitor;

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

    private void acceptMain(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    @Override
    public void accept(Visitor v){
        if (v instanceof TypeCheckVisitor){
            TypeCheckVisitor visitor = (TypeCheckVisitor)v;
            visitor.context.entry("Parameter List");
            acceptMain(v);
            visitor.context.pop();
        }   else{
            acceptMain(v);
        }
    }
}
