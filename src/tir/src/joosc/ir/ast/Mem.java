package tir.src.joosc.ir.ast;

import backend.asm.*;
import exception.BackendError;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

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
        canonicalized_node = new Seq(((Expr_c)(expr)).canonicalized_node.stmts());
        canonicalized_node.setLastStatement(new Exp(new Mem(canonicalized_node.getLastExpr())));
    }

    public mem toAsmMem(){
        if (expr instanceof Temp){
            return new mem(new Register(((Temp)expr).name()));
        }   else if (expr instanceof BinOp){
            BinOp binOp = (BinOp)expr;
            if (binOp.left() instanceof Temp && binOp.right() instanceof Const){
                return new mem(new Register(((Temp)binOp.left()).name()), binOp.opType(), new backend.asm.Const(((Const)binOp.right()).value()));
            }   else if (binOp.left() instanceof Temp && binOp.right() instanceof Temp){
                return new mem(new Register(((Temp)binOp.left()).name()), binOp.opType(),new Register(((Temp)binOp.left()).name()));
            }   else if (binOp.left() instanceof Temp && binOp.right() instanceof BinOp){
                BinOp _binop = (BinOp)binOp.right();
                Register reg1 = new Register(((Temp)binOp.left()).name());
                Register reg2 = new Register(((Temp)(_binop.left())).name());
                backend.asm.Const c3 =  new backend.asm.Const(((Const)(_binop.right())).value());
                return new mem(reg1, binOp.opType(), reg2,_binop.opType(), c3, null, null);
            }
        }
        throw new BackendError("unknown toAsmMem" + this);
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Node>nodes = new ArrayList<Node>();
        Tile t = v.unit();
        t.codes.add(new mov(res_register, toAsmMem()));
        return new Pair<List<Node>, Tile>(nodes, t);
    }
}
