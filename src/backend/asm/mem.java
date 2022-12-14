package backend.asm;

import exception.BackendError;
import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

public class mem extends Operand{
    public static String OpToChar(BinOp.OpType op){
        if (op == null) return "";
        switch (op){
            case SUB: return "-";
            case ADD: return "+";
            case MUL: return "*";
            case DIV: return "/";
            default: return "+"; // for now fixme
        }
    }

    public static String RegToStr(Register t){
        if (t == null) return "";
        return t.toString();
    }

    public static String ConstToStr(Const c){
        if (c == null) return "";
        return c.toString();
    }

    Register t1 = null;
    Register t2 = null;
    Const c1 = null;
    Const c2 = null;
    Const c3 = null;
    Const c4 = null;
    BinOp.OpType operator1 = null;
    BinOp.OpType operator2 = null;
    BinOp.OpType operator3 = null;

    public mem(Operand t1, BinOp.OpType op, Operand t2){
        if (t1 instanceof Register){
            this.t1 = (Register)t1;
        }   else {
            throw new BackendError("cannot have address of non-reg");
        }
        this.operator1 = op;

        if (t2 instanceof Register){
            this.t2 = (Register)t2;
        }   else {
            this.c2 = (Const)t2;
        }
    }

    public mem(Operand t1, BinOp.OpType op, Operand t2, BinOp.OpType op2, Const t3,BinOp.OpType op3, Const t4 ){
        if (t1 instanceof Register){
            this.t1 = (Register)t1;
        }   else {
            throw new BackendError("cannot have address of non-reg");
        }

        this.operator1 = op;

        if (t2 instanceof Register){
            this.t2 = (Register)t2;
        }   else {
            this.c2 = (Const)t2;
        }

        this.operator2 = op2;
        this.c3 = t3;
        this.operator3 = op3;
        this.c4 = t4;
    }

    public mem(Operand t1){
        if (t1 instanceof Register){
            this.t1 = (Register)t1;
        }   else {
            this.c1 = (Const)t1;
        }

        this.operator1 = null;
        c2 = null;
        t2 = null;
    }

    @Override
    public String toString() {
        if (operator1 == null) {
            return "[ " + RegToStr(t1) + ConstToStr(c1) + " ]";
        }
        return "[ " + RegToStr(t1) + ConstToStr(c1) + OpToChar(operator1) + ConstToStr(c2) + RegToStr(t2)  + OpToChar(operator2) + ConstToStr(c3) + OpToChar(operator3) + ConstToStr(c4) + " ]";
    }

    public static mem genVarAccessMem(FuncDecl funcDecl, String name){
        return new mem(Register.ebp, BinOp.OpType.SUB, new Const(funcDecl.getOffset(name)));
    }



}
