package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class UnaryOpCodeL extends UnaryOpCode{
    public UnaryOpCodeL(Operand op) {
        super(op);
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
                op = Register.ecx;
                res.add(this);
                return res;
            }
            res.add(this);
            return res;
        }
    }
}
