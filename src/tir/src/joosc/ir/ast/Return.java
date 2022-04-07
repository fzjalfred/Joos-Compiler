package tir.src.joosc.ir.ast;

import backend.asm.Register;
import backend.asm.Tile;
import backend.asm.mov;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/** RETURN statement */
public class Return extends Statement {
    protected Expr ret;

    /**
     * @param ret values to return
     */
    public Return(Expr ret) {
        this.ret = ret;
    }

    public Expr ret() {
        return ret;
    }

    @Override
    public String label() {
        return "RETURN";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        boolean modified = false;

        Expr newExpr = (Expr) v.visit(this, ret);
        if (newExpr != ret)
            modified = true;
        Expr result = newExpr;

        if (modified)
            return v.nodeFactory().IRReturn(result);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(ret));
        return result;
    }

    @Override
    public String toString() {
        return "Return{" +
                "expr=" + ret +
                '}';
    }

    @Override
    public void canonicalize() {
        Seq ret_can = new Seq(((Expr_c)ret).canonicalized_node.stmts());
        ret_can.setLastStatement(new Return(ret_can.getLastExpr()));
        canonicalized_node = ret_can;
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Node> nodes = new ArrayList<Node>();
        Tile codes = v.unit();;
        return new Pair<List<Node>, Tile>(nodes,codes);
    }
}
