package ast;

public class ConditionalStmt implements AtomicStmt{
    public Expr expr;

    public ConditionalStmt(Expr expr){
        this.expr = expr;
    }

    // @Override
    // public String toString() {
    //     // TODO Auto-generated method stub
    //     return "ConditionalStmt" + expr;
    // }
}
