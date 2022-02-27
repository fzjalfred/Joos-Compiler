package ast;

import visitors.Visitor;

import java.util.List;

public class ConstructorBody extends ASTNode {
    public ConstructorBody(List<ASTNode> children, String value){
        super(children, value);
    }

    public BlockStmts getBlockStmts(){
        assert children.get(0) instanceof BlockStmts;
        return (BlockStmts)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}