package ast;

import java.util.List;

public class IfThenElseStmt extends Stmt {
    public IfThenElseStmt(List<ASTNode> children, String value){
        super(children, value);
    }
    public Expr getExpr(){
        assert children.get(0) instanceof Expr;
        return (Expr)children.get(0);
    }
    public BlockStmt getThenStmt(){
        assert children.get(1) instanceof BlockStmt;
        return (BlockStmt)children.get(1);
    }
    public BlockStmt getElseStmt(){
        assert children.get(2) instanceof BlockStmt;
        return (BlockStmt)children.get(2);
    }
}