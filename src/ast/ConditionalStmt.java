package ast;

public class ConditionalStmt implements AtomicStmt{
    public Expr expr;

    public ConditionalStmt(Expr expr){
        this.expr = expr;
    }
}
