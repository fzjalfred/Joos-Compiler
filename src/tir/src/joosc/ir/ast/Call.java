package tir.src.joosc.ir.ast;

import backend.asm.*;
import backend.asm.Const;
import tir.src.joosc.ir.interpret.Configuration;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An intermediate representation for a function call
 * CALL(e_target, e_1, ..., e_n)
 */
public class Call extends Expr_c {
    public String funcLabel;
    protected Expr target;
    protected List<Expr> args;
    public Temp returnTarget;

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
            funcLabel = "NATIVEjava.io.OutputStream.nativeWrite";
            return true;
        } else if (name_str.equals("__malloc")) {
            funcLabel = "__malloc";
            return true;
        } else if (name_str.equals("__debexit")) {
            funcLabel = "__debexit";
            return true;
        } else if (name_str.equals("__exception")) {
            funcLabel = "__exception";
            return true;
        }
        return false;
    }

    private boolean isSyscall(String name_str) {
        if (name_str.equals("NATIVEjava.io.OutputStream.nativeWrite")) {
            funcLabel = "NATIVEjava.io.OutputStream.nativeWrite";
            return true;
        } else if (name_str.equals("__malloc")) {
            funcLabel = "__malloc";
            return true;
        } else if (name_str.equals("__debexit")) {
            funcLabel = "__debexit";
            return true;
        } else if (name_str.equals("__exception")) {
            funcLabel = "__exception";
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
        Call newCall = null;
        if (sys_flag) {
            newCall = new Call(target, temp_list);
        } else {
            newCall = new Call(name_t, temp_list);
        }
        newCall.funcLabel = funcLabel;
        newCall.returnTarget = returnTarget;
        seq.addStatement(new Exp(newCall));

        seq.addStatement(new Exp(new Temp(Configuration.ABSTRACT_RET)));
        canonicalized_node = seq;
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        int argNum = args.size();
        boolean sysflag = false;
        if (funcLabel != null && isSyscall(funcLabel)) {
            sysflag = true;
        }

        Tile res = v.unit();
        for (Expr arg : args) {
            Tile argTile = v.unit();
            List<Code> argCodes = new ArrayList<Code>();
            if (arg instanceof Temp) {
                if (sysflag) {
                    argCodes.add(new mov(Register.eax, new Register(((Temp)arg).name())));
                }   else {
                    argCodes.add(new push(new Register(((Temp)arg).name())));
                }
            } else {
                // T[e]t
                Register t = null;
                if (sysflag) {
                    t = Register.eax;
                } else {
                    t = RegFactory.getRegister();
                }

                arg.setResReg(t);
                argTile =  v.visit(arg);

                // push
                argCodes.add(new push(t));

            }
            Tile argRes = v.bind(argTile, new Tile(argCodes));

            res = v.bind(argRes, res); // in reverse order
        }
//        System.out.println(res);

        List<Code> tileCodes = new ArrayList<Code>();
        if (target instanceof Temp){
            tileCodes.add(new call(new Register(((Temp)target).name())));
        }   else {
            tileCodes.add(new call(new LabelOperand(((Name)target).name())));
        }

        if (returnTarget != null) {
            tileCodes.add(new mov(new Register(returnTarget.name()), Register.eax));
        }

        tileCodes.add(new add(Register.esp, new Const(4*argNum)));
        res = v.bind(res, new Tile(tileCodes));
        return new Pair<>(new ArrayList<Node>(), res);
    }
}
