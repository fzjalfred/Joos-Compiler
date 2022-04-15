package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class pop extends UnaryOpCodeS{
    public pop(Operand operand){
        super(operand);
    }

}
