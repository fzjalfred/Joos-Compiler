package backend.asm;

import tir.src.joosc.ir.ast.BinOp;

public class lea extends BinaryOpCode{
    public enum OpType {
        ADD, SUB, MUL, DIV
    }

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
    public static class leaOp2 extends Operand{
        Register t1 = null;
        Register t2 = null;
        Const c1 = null;
        Const c2 = null;
        BinOp.OpType operator1 = null;


        public leaOp2(Operand t1, BinOp.OpType op, Operand t2){
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



        @Override
        public String toString() {
            return "[ " + RegToStr(t1) + ConstToStr(c1) + OpToChar(operator1) + ConstToStr(c2) + RegToStr(t2) + " ]";
        }
    }
    public lea(Operand op1, Operand op2){
        super(op1, op2);
    }
}
