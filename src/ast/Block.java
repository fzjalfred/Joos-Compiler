package ast;

import visitors.*;

import java.util.List;

public class Block extends StmtWithoutSubstmt {
    public Block(List<ASTNode> children, String value){
        super(children, value);
    }
    public BlockStmts getBlockStmts(){
        assert children.get(0) instanceof BlockStmts;
        return (BlockStmts)children.get(0);
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
            visitor.context.entry();
            acceptMain(v);
            visitor.context.pop();
        }   else{
            acceptMain(v);
        }
    }
}