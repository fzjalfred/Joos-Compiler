package backend.asm;

import tir.src.joosc.ir.ast.BinOp;

public class mem extends Operand{
    public Operand op;

    //TODO: tostring
    public mem(BinOp binOp){

    }

    public mem(Const num){
        op = num;
    }

    public mem(Register reg){
        op = reg;
    }
}
