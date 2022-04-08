package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.ast.Label;

import java.util.ArrayList;
import java.util.List;

public class call extends UnaryOpCode{
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

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        if (op instanceof LabelOperand){
            return super.regAllocate(funcDecl);
        }   else {
            List<Code> res = new ArrayList<Code>();
            Register _t = (Register)op;
            if (Register.isAbstractRegister(_t)){
                res.add(new mov(Register.ecx, mem.genVarAccessMem(funcDecl, _t.name)));
                res.add(new call(Register.ecx));
                return res;
            }
            res.add(this);
            return res;
        }
    }
}
