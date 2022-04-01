package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.interpret.Configuration;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An intermediate representation for a function call
 * CALL(e_target, e_1, ..., e_n)
 */
public class Call extends Expr_c {
    protected Expr target;
    protected List<Expr> args;

    /**
     *
     * @param target address of the code for this function call
     * @param args arguments of this function call
     */
    public Call(Expr target, Expr... args) {
        this(target, Arrays.asList(args));
    }

    /**
     *
     * @param target address of the code for this function call
     * @param numRets number of return values for this function call
     * @param args arguments of this function call
     */
    public Call(Expr target, List<Expr> args) {
        this.target = target;
        this.args = args;
    }

    public Expr target() {
        return target;
    }

    public List<Expr> args() {
        return args;
    }

    public int getNumArgs() {
        return args.size();
    }

    private boolean isSyscall(Name name) {
        String name_str = name.name();
        if (name_str.equals("NATIVEjava.io.OutputStream.nativeWrite")) {
            return true;
        } else if (name_str.equals("__malloc")) {
            return true;
        } else if (name_str.equals("__debexit")) {
            return true;
        } else if (name_str.equals("__exception")) {
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "CALL";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        boolean modified = false;

        Expr target = (Expr) v.visit(this, this.target);
        if (target != this.target) modified = true;

        List<Expr> results = new ArrayList<>(args.size());
        for (Expr arg : args) {
            Expr newExpr = (Expr) v.visit(this, arg);
            if (newExpr != arg) modified = true;
            results.add(newExpr);
        }

        if (modified) return v.nodeFactory().IRCall(target, results);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(target));
        for (Expr arg : args)
            result = v.bind(result, v.visit(arg));
        return result;
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return !v.inExpr();
    }

    @Override
    public String toString() {
        return "Call{" +
                "function=" + target + ", " +
                "args=" + args +
                '}';
    }

    @Override
    public void canonicalize() {
        List<Statement> stmts = new ArrayList<Statement>();
        Seq seq = new Seq(stmts);
        List<Expr> temp_list = new ArrayList<Expr>();
        Temp name_t = new Temp("t_"+hashCode());

        boolean sys_flag = false;
        // Name
        if (target instanceof Name && isSyscall((Name)target)) {
            sys_flag = true;
        } else {
            Seq name_seq = new Seq(((Expr_c)target).canonicalized_node.stmts());
            name_seq.setLastStatement(new Move(name_t, name_seq.getLastExpr()));
            seq.addSeq(name_seq);
        }


        // Arguments
        int index = 0;

        for (Expr arg : args) {
            Temp t = new Temp("t"+index+"_"+hashCode());
            Seq arg_seq = new Seq(((Expr_c)arg).canonicalized_node.stmts());
            arg_seq.setLastStatement(new Move(t, arg_seq.getLastExpr()));
            seq.addSeq(arg_seq);
            temp_list.add(t);
            index++;
        }

        if (sys_flag) {
            seq.addStatement(new Exp(new Call(target, temp_list)));
        } else {
            seq.addStatement(new Exp(new Call(name_t, temp_list)));
        }

        seq.addStatement(new Exp(new Temp(Configuration.ABSTRACT_RET)));
        canonicalized_node = seq;
    }
}
