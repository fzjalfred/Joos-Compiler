package ast;

import visitors.TypeCheckVisitor;
import visitors.UnreachableStmtVisitor;
import visitors.Visitor;

import java.util.List;

public class ForStmt extends Stmt {
    public ForStmt(List<ASTNode> children, String value){
        super(children, value);
    }
    public ForInit getForInit(){
        assert children.get(0) instanceof ForInit;
        return (ForInit)children.get(0);
    }
    public ForUpdate getForUpdate(){
        assert children.get(2) instanceof ForUpdate;
        return (ForUpdate)children.get(2);
    }
    public Expr getForExpr(){
        assert children.get(1) instanceof Expr;
        return (Expr)children.get(1);
    }
    public BlockStmt getBlockStmt(){
        assert children.get(3) instanceof BlockStmt;
        return (BlockStmt)children.get(3);
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
            visitor.context.entry("ForStmt");
            acceptMain(v);
            visitor.context.pop();
        } else if(v instanceof UnreachableStmtVisitor) {
            v.visit(this);
        }  else{
            acceptMain(v);
        }
    }
}