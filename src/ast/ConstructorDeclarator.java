package ast;

import visitors.Visitor;

import java.util.List;

public class ConstructorDeclarator extends ASTNode {
    public ConstructorDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(0).value;
    }
    public ParameterList getParameterList(){
        assert children.get(1) instanceof ParameterList;
        return (ParameterList)children.get(1);
    }

    public List<Type> getParamType(){
        if (getParameterList() == null) return null;
        return getParameterList().getParamType();
    }
    public int numParams(){
        if (getParameterList() == null) return 0;
        return getParameterList().paramNum();
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}