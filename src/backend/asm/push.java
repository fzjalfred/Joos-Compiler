package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class push extends UnaryOpCode{
    public push(Operand operand){
        super(operand);
    }

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        if (op instanceof Register){
            Register _reg = (Register)op;
            List<Code> res = new ArrayList<Code>();
            if (Register.eax.isAbstractRegister(_reg)){
                int offset =funcDecl.getOffset(_reg.name);
                res.add(new mov(Register.ecx, new mem(Register.ebp, BinOp.OpType.SUB, new Const(offset))));
                res.add(new push(Register.ecx));
            }   else {
                res.add(this);
            }
            return res;
        }
        return null;
    }
}
