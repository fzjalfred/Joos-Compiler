package ast;

import tir.src.joosc.ir.ast.Statement;
import visitors.Visitor;

import java.util.List;

public class ConstructorDeclarator extends ASTNode {
    public List<Statement> ir_node;
    public ConstructorDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public ConstructorDecl constructorDecl;
    public String getName(){
        return children.get(0).value;
    }
    public ParameterList getParameterList(){
        assert children.get(1) instanceof ParameterList;
        return (ParameterList)children.get(1);
    }

    public boolean hasParameterList(){
        if (children.get(1) == null){
            return false;
        }
        return true;
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