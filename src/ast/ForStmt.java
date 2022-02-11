package ast;

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
}