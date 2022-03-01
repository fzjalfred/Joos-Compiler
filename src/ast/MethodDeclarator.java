package ast;

import visitors.Visitor;

import java.util.List;

public class MethodDeclarator extends ASTNode {
    public MethodDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(0).value;
    }
    public boolean hasParameterList() {
        if (children.get(1) == null) {
            return false;
        }
        return true;
    }
    public ParameterList getParameterList(){
        assert children.get(1) instanceof ParameterList;
        return (ParameterList)children.get(1);
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