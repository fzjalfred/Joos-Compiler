package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;

/**
 * An intermediate representation for a memory location
 * MEM(e)
 */
public class Mem extends Expr_c {

    private Expr expr;

    /**
     *
     * @param expr the address of this memory location
     */
    public Mem(Expr expr) {
        this.expr = expr;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public String label() {
        return "MEM";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Expr expr = (Expr) v.visit(this, this.expr);

        if (expr != this.expr) return v.nodeFactory().IRMem(expr);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(expr));
        return result;
    }
    @Override
    public String toString() {
        return "Mem{" +
                "expr=" + expr +
                '}';
    }

    @Override
    public void canonicalize() {
        canonicalized_node = ((Expr_c)(expr)).canonicalized_node;
        canonicalized_node.setLastStatement(new Exp(new Mem(canonicalized_node.getLastExpr())));
    }
}
