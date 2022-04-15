package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class push extends UnaryOpCodeL{
    public push(Operand operand){
        super(operand);
    }

}
