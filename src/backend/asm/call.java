package backend.asm;

import tir.src.joosc.ir.ast.Label;

public class call extends UnaryOpCode{
    public call(LabelOperand labelOp) {
        super(labelOp);
    }

    @Override
    public  String toString() {
        return "call " + op;
    }
}
