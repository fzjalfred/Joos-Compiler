package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class jmp extends UnaryOpCodeL{

    public jmp(LabelOperand labelOp){
        super(labelOp);
    }

    public jmp(Register labelOp){
        super(labelOp);
    }

    @Override
    public String toString() {
        return "jmp " + op;
    }

}
