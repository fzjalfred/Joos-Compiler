package ast;

import java.util.List;

public class WhileStmt extends Stmt {
    public WhileStmt(List<ASTNode> children, String value){
        super(children, value);
    }
    public Expr getExpr(){
        assert children.get(0) instanceof Expr;
        return (Expr)children.get(0);
    }
    public BlockStmt getStmt(){
        assert children.get(1) instanceof BlockStmt;
        return (BlockStmt)children.get(1);
    }
}