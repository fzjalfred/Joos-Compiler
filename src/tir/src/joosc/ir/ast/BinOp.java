package tir.src.joosc.ir.ast;

import backend.asm.*;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate representation for a binary operation
 * OP(left, right)
 */
public class BinOp extends Expr_c {

    /**
     * Binary operators
     */
    public enum OpType {
        ADD, SUB, MUL, DIV, MOD, AND, OR, XOR, LSHIFT, RSHIFT, ARSHIFT,
        EQ, NEQ, LT, GT, LEQ, GEQ
    }
    public boolean isComparsionOp(OpType op){
        return (op == OpType.EQ || op == OpType.NEQ || op == OpType.LT || op == OpType.GT || op == OpType.LEQ || op == OpType.GEQ);
    }

    private OpType type;
    private Expr left, right;

    public BinOp(OpType type, Expr left, Expr right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    public OpType opType() {
        return type;
    }

    public Expr left() {
        return left;
    }

    public Expr right() {
        return right;
    }

    @Override
    public String label() {
        return type.toString();
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Expr left = (Expr) v.visit(this, this.left);
        Expr right = (Expr) v.visit(this, this.right);

        if (left != this.left || right != this.right)
            return v.nodeFactory().IRBinOp(type, left, right);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(left));
        result = v.bind(result, v.visit(right));
        return result;
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public String toString() {
        return "BinOp{" +
                "type=" + type +
                ", left=" + left +
                ", right=" + right +
                '}';
    }

    @Override
    public void canonicalize() {
        Seq eq_can1 = new Seq(((Expr_c)left).canonicalized_node.stmts());
        Seq eq_can2 = new Seq(((Expr_c)right).canonicalized_node.stmts());
        Temp t1 = new Temp("t"+hashCode());
        eq_can1.setLastStatement(new Move(t1, eq_can1.getLastExpr()));
        eq_can2.setLastStatement(new Exp(new BinOp(type, t1, eq_can2.getLastExpr())));
        eq_can1.addSeq(eq_can2);
        canonicalized_node = eq_can1;
    }

    void processOpright(List<Code> codes, List<Node> nodes,Operand t1, Node node){
        if (node instanceof Temp){
            codes.add(new lea(res_register, new mem(t1, type, new Register(((Temp)node).name()))));
        }   else if (right instanceof Const){
            codes.add(new lea(res_register, new mem(t1, type, new backend.asm.Const(((Const)right).value()))));
        }   else {
            Register t2 = RegFactory.getRegister();
            codes.add(new lea(res_register, new mem(t1, type, t2)));
            right.setResReg(t2);
            nodes.add(right);
        }
    }
    setcc genSetInst(OpType type, Register res){
        if (type == OpType.EQ) return new setcc(setcc.ccType.e, res);
        if (type == OpType.NEQ) return new setcc(setcc.ccType.ne, res);
        if (type == OpType.LEQ) return new setcc(setcc.ccType.le, res);
        if (type == OpType.GEQ) return new setcc(setcc.ccType.ge, res);
        if (type == OpType.GT) return new setcc(setcc.ccType.g, res);
        if (type == OpType.LT) return new setcc(setcc.ccType.l, res);
        return null;
    }

    void processOprightCmp(List<Code> codes, List<Node> nodes,Operand t1, Node node){
        if (node instanceof Temp){
            Register reg2 = new Register(((Temp)node).name());
            codes.add(new cmp(t1, reg2));
            codes.add(genSetInst(type, res_register));
        }   else if (right instanceof Const){
            backend.asm.Const c2 = new backend.asm.Const(((Const)right).value());
            codes.add(new cmp(t1, c2));
            codes.add(genSetInst(type, res_register));
        }   else {
            Register reg2 = RegFactory.getRegister();
            codes.add(new cmp(t1, reg2));
            codes.add(genSetInst(type, res_register));
            right.setResReg(reg2);
            nodes.add(right);
        }
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Code> codes = new ArrayList<Code>();
        List<Node> nodes = new ArrayList<Node>();
        if (isComparsionOp(type)){
            codes.add(new xor(res_register, res_register));
            if (left instanceof Temp){
                Register t1 = new Register(((Temp)left).name());
                processOprightCmp(codes, nodes, t1, right);
            }   else if (left instanceof Const){
                backend.asm.Const t1 = new backend.asm.Const(((Const)left).value());
                processOprightCmp(codes, nodes, t1, right);
            }   else {
                Register t1 = RegFactory.getRegister();
                left.setResReg(t1);
                nodes.add(left);
                processOprightCmp(codes, nodes, t1, right);
            }
        }   else {
            if (left instanceof Temp){
                Register t1 = new Register(((Temp)left).name());
                processOpright(codes, nodes, t1, right);
            }   else if (left instanceof Const){
                backend.asm.Const t1 = new backend.asm.Const(((Const)left).value());
                processOpright(codes, nodes, t1, right);
            }   else {
                Register t1 = RegFactory.getRegister();
                left.setResReg(t1);
                nodes.add(left);
                processOpright(codes, nodes, t1, right);
            }
        }

        return new Pair<List<Node>, Tile>(nodes, new Tile(codes));
    }
}
