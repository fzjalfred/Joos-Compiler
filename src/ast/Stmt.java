package ast;

import tir.src.joosc.ir.ast.Statement;
import visitors.Visitor;

import java.util.List;

public class Stmt extends BlockStmt {
    public Statement ir_node;
    public Stmt(List<ASTNode> children, String value){
        super(children, value);
        ir_node = null;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
