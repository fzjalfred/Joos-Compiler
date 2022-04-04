package tir.src.joosc.ir.ast;

import backend.asm.*;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate representation for a move statement
 * MOVE(target, expr)
 */
public class Move extends Statement {
    private Expr target;
    private Expr src;

    /**
     *
     * @param target the destination of this move
     * @param src the expression whose value is to be moved
     */
    public Move(Expr target, Expr src) {
        this.target = target;
        this.src = src;
    }

    public Move(Expr target, Expr src, boolean trash) {
        this.target = target;
        this.src = src;
    }

    public Expr target() {
        return target;
    }

    public Expr source() {
        return src;
    }

    @Override
    public String label() {
        return "MOVE";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Expr target = (Expr) v.visit(this, this.target);
        Expr expr = (Expr) v.visit(this, src);

        if (target != this.target || expr != src)
            return v.nodeFactory().IRMove(target, expr);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(target));
        result = v.bind(result, v.visit(src));
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "target=" + target +
                ", src=" + src +
                '}';
    }

    @Override
    public void canonicalize() {
        if (target instanceof Mem) {
            Seq e1_can = new Seq(((Expr_c)((Mem)target).expr()).canonicalized_node.stmts());
            Temp t1 = new Temp("t"+hashCode());
            e1_can.setLastStatement(new Move(t1, e1_can.getLastExpr()));
            Seq e2_can = new Seq(((Expr_c)src).canonicalized_node.stmts());
            e2_can.setLastStatement(new Move(new Mem(t1), e2_can.getLastExpr()));
            e1_can.addSeq(e2_can);
            canonicalized_node = e1_can;
        } else {
            Seq e2_can = new Seq(((Expr_c)src).canonicalized_node.stmts());
            e2_can.setLastStatement(new Move(target, e2_can.getLastExpr()));
            canonicalized_node = e2_can;
        }

    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Code> tileCodes = new ArrayList<Code>();
        List<Node> nodes = new ArrayList<Node>();
        Operand operand2 = null;
        if (src instanceof Const){
            operand2 = new backend.asm.Const(((Const)src).value());
        }   else if (src instanceof Temp){
            operand2 = new Register(((Temp)src).name());
        }   else {
            Register srcReg = RegFactory.getRegister();
            operand2 = srcReg;
            nodes.add(src);
            src.setResReg(srcReg);
        }
        Operand operand1 = null;
        if (target instanceof Temp){
            operand1 = new Register(((Temp)target).name());
            tileCodes.add(new mov(operand1, operand2));

            //nodes.add(target); NO need to visit these nodes
            //nodes.add(src);
            return new Pair<List<Node>, Tile>(nodes, new Tile(tileCodes));
        }   else {
            return null;        //TODO: now only support move to register, fixme
        }
    }
}
