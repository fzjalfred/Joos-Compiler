package tir.src.joosc.ir.ast;

import backend.asm.Code;
import backend.asm.RegFactory;
import backend.asm.Register;
import backend.asm.Tile;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate representation for evaluating an expression for side effects,
 * discarding the result
 * EXP(e)
 */
public class Exp extends Statement {
    private Expr expr;

    /**
     *
     * @param expr the expression to be evaluated and result discarded
     */
    public Exp(Expr expr) {
        this.expr = expr;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public String label() {
        return "EXP";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Expr expr = (Expr) v.visit(this, this.expr);

        if (expr != this.expr) return v.nodeFactory().IRExp(expr);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(expr));
        return result;
    }

    @Override
    public CheckCanonicalIRVisitor checkCanonicalEnter(
            CheckCanonicalIRVisitor v) {
        return v.enterExp();
    }

    @Override
    public void canonicalize() {
        List<Statement> stmts = new ArrayList<Statement>();
        List<Statement> stmts_e = ((Expr_c)(expr)).canonicalized_node.stmts();
        for (int i = 0; i < stmts_e.size()-1; ++i){
            stmts.add(stmts_e.get(i));
        }
        canonicalized_node = new Seq(stmts);
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Node> nodes = new ArrayList<Node>();

        Register t = RegFactory.getRegister();
        expr.setResReg(t);
        nodes.add(expr);

        return new Pair<>(nodes, v.unit());
    }

    @Override
    public String toString() {
        return "Exp{" +
                "expr=" + expr +
                '}';
    }
}
