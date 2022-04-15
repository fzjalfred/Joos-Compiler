package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.ast.Label;

import java.util.ArrayList;
import java.util.List;

public class call extends UnaryOpCodeL{
    public call(LabelOperand labelOp) {
        super(labelOp);
    }
    public call(Register labelOp) {
        super(labelOp);
    }

    @Override
    public  String toString() {
        return "call " + op;
    }

}
