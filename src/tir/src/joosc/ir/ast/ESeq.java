package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;

/**
 * An intermediate representation for an expression evaluated under side effects
 * ESEQ(stmt, expr)
 */
public class ESeq extends Expr_c {
    private Statement statement;
    private Expr expr;

    /**
     * @param statement IR statement to be evaluated for side effects
     * @param expr IR expression to be evaluated after {@code stmt}
     */
    public ESeq(Statement statement, Expr expr) {
        this.statement = statement;
        this.expr = expr;
    }

    public ESeq(Statement statement, Expr expr, boolean replaceParent) {
        this.statement = statement;
        this.expr = expr;
    }

    public Statement stmt() {
        return statement;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public String label() {
        return "ESEQ";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Statement statement = (Statement) v.visit(this, this.statement);
        Expr expr = (Expr) v.visit(this, this.expr);

        if (expr != this.expr || statement != this.statement)
            return v.nodeFactory().IRESeq(statement, expr);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(statement));
        result = v.bind(result, v.visit(expr));
        return result;
    }

    @Override
    public String toString() {
        return "Eseq{" +
                "statement=" + statement + ", " +
                "expr=" + expr +
                '}';
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return false;
    }


    @Override
    public void canonicalize() {
        canonicalized_node = statement.canonicalized_node;
        canonicalized_node.addSeq(((Expr_c)expr).canonicalized_node);
    }
}
