package backend.asm;

import tir.src.joosc.ir.ast.BinOp;

public class mem extends Operand{
    public static String OpToChar(BinOp.OpType op){
        switch (op){
            case SUB: return "-";
            case ADD: return "+";
            case MUL: return "*";
            case DIV: return "/";
            default: return "";
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
    BinOp.OpType operator1 = null;

    public mem(Operand t1, BinOp.OpType op, Operand t2){
        if (t1 instanceof Register){
            this.t1 = (Register)t1;
        }   else {
            this.c1 = (Const)t1;
        }

        this.operator1 = op;

        if (t2 instanceof Register){
            this.t2 = (Register)t2;
        }   else {
            this.c2 = (Const)t2;
        }
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
        return "[ " + RegToStr(t1) + ConstToStr(c1) + OpToChar(operator1) + ConstToStr(c2) + RegToStr(t2) + " ]";
    }



}
