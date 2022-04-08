package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class pop extends UnaryOpCode{
    public pop(Operand operand){
        super(operand);
    }

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        if (op instanceof Register){
            Register _reg = (Register)op;
            List<Code> res = new ArrayList<Code>();
            if (Register.eax.isAbstractRegister(_reg)){
                int offset =funcDecl.getOffset(_reg.name);
                res.add(new pop(Register.ecx));
                res.add(new mov(new mem(Register.ebp, BinOp.OpType.SUB, new Const(offset)), Register.ecx));
            }   else {
                res.add(this);
            }
            return res;
        }
        return null;
    }
}
