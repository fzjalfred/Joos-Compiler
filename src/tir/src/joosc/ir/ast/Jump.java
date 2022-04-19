package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;


import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;
import backend.asm.*;
import java.util.ArrayList;
import java.util.List;
/**
 * An intermediate representation for a transfer of control
 */
public class Jump extends Statement {
    private Expr target;

    /**
     *
     * @param expr the destination of the jump
     */
    public Jump(Expr expr) {
        target = expr;
    }

    public Expr target() {
        return target;
    }

    @Override
    public String label() {
        return "JUMP";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Expr expr = (Expr) v.visit(this, target);

        if (expr != target) return v.nodeFactory().IRJump(expr);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(target));
        return result;
    }
    @Override
    public String toString() {
        return "Jump{" +
                "target=" + target +
                '}';
    }

    @Override
    public void canonicalize() {
        Seq res  = new Seq(((Expr_c)target).canonicalized_node.stmts());
        res.setLastStatement(new Jump(res.getLastExpr()));
        canonicalized_node = res;
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Code> tileCodes = new ArrayList<Code>();
        List<Node> nodes = new ArrayList<Node>();
        if (target instanceof Temp) {
            tileCodes.add(new jmp(Register.tempToReg((Temp)target)));
        } else if (target instanceof Name) {
            tileCodes.add(new jmp(new LabelOperand(((Name)target).name())));
        }
        return new Pair<List<Node>,Tile>(nodes, new Tile(tileCodes));
    }
}
