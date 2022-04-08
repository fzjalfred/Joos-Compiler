package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;

import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;
import backend.asm.*;
import java.util.ArrayList;
import java.util.List;
/**
 * An intermediate representation for a conditional transfer of control
 * CJUMP(expr, trueLabel, falseLabel)
 */
public class CJump extends Statement {
    private Expr cond;
    private String trueLabel, falseLabel;

    /**
     * Construct a CJUMP instruction with fall-through on false.
     * @param cond the condition for the jump
     * @param trueLabel the destination of the jump if {@code expr} evaluates
     *          to true
     */
    public CJump(Expr cond, String trueLabel) {
        this(cond, trueLabel, null);
    }

    /**
     *
     * @param cond the condition for the jump
     * @param trueLabel the destination of the jump if {@code expr} evaluates
     *          to true
     * @param falseLabel the destination of the jump if {@code expr} evaluates
     *          to false
     */
    public CJump(Expr cond, String trueLabel, String falseLabel) {
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public Expr cond() {
        return cond;
    }

    public String trueLabel() {
        return trueLabel;
    }

    public String falseLabel() {
        return falseLabel;
    }

    public boolean hasFalseLabel() {
        return falseLabel != null;
    }

    @Override
    public String label() {
        return "CJUMP";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Expr expr = (Expr) v.visit(this, this.cond);

        if (expr != this.cond)
            return v.nodeFactory().IRCJump(expr, trueLabel, falseLabel);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(cond));
        return result;
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return !hasFalseLabel();
    }

    @Override
    public String toString() {
        return "Cjump{" +
                "cond=" + cond + ", " +
                "true=" + trueLabel + ", " +
                "false=" + falseLabel +
                '}';
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq(new CJump(cond, trueLabel), new Jump(new Name(falseLabel)));
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Code> tileCodes = new ArrayList<Code>();
        List<Node> nodes = new ArrayList<Node>();
        if (cond instanceof BinOp && ((BinOp)cond).opType() ==BinOp.OpType.EQ) {
            Register left_reg = RegFactory.getRegister();
            Register right_reg = RegFactory.getRegister(); 
            ((BinOp)cond).left().setResReg(left_reg);
            nodes.add(((BinOp)cond).left());
            ((BinOp)cond).right().setResReg(right_reg);
            nodes.add(((BinOp)cond).right());
            tileCodes.add(new cmp(left_reg, right_reg));
            tileCodes.add(new jcc(jcc.ccType.e, new LabelOperand(trueLabel)));
            return new Pair<List<Node>, Tile>(nodes, new Tile(tileCodes)); 
        } else {
            Register cond_reg = RegFactory.getRegister();
            nodes.add(cond);
            cond.setResReg(cond_reg);
            tileCodes.add(new test(cond_reg, cond_reg));
            tileCodes.add(new jcc(jcc.ccType.nz, new LabelOperand(trueLabel)));
            return new Pair<List<Node>,Tile>(nodes, new Tile(tileCodes));
        }
    }
}
