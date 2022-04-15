package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class lea extends mov{

    public static String OpToChar(BinOp.OpType op){
        switch (op){
            case SUB: return "-";
            case ADD: return "+";
            case MUL: return "*";
            case DIV: return "/";
            default: {
                return "+";
            }
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

    public lea(Operand op1, Operand op2){
        super(op1, op2);
    }

}
