package ast;

import visitors.Visitor;

import java.util.List;

public class MethodBody extends ASTNode {
    public MethodBody(List<ASTNode> children, String value){
        super(children, value);
    }
    public Block getBlock(){
        assert children.get(0) instanceof Block;
        return (Block)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
