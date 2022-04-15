package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.ast.Mem;

import java.util.ArrayList;
import java.util.List;

public abstract class BinaryOpCode extends Code {
    public Operand op1;
    public Operand op2;
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + op1 + "," + op2;
    }

    public BinaryOpCode(Operand op1, Operand op2){
        this.op1=op1;
        this.op2=op2;
    }




}
