package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class jmp extends UnaryOpCode{

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

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        List<Code> res = new ArrayList<Code>();
        if (op instanceof Register && Register.isAbstractRegister((Register)op)){
            res.add(new mov(Register.ecx, mem.genVarAccessMem(funcDecl, ((Register)op).name)));
            res.add(new jmp(Register.ecx));
            return res;
        }
        res.add(this);
        return res;
    }
}
