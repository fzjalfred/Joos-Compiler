package ast;

import visitors.Visitor;

import java.util.List;

public class Parameter extends ASTNode implements Referenceable {
    public Parameter(List<ASTNode> children, String value){
        super(children, value);
    }
    public VarDeclaratorID getVarDeclaratorID(){
        assert children.get(1) instanceof VarDeclaratorID;
        return (VarDeclaratorID)children.get(1);
    }

    @Override
    public Type getType(){
        assert children.get(0) instanceof Type;
        return (Type)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}