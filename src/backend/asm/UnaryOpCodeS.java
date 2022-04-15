package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class UnaryOpCodeS extends UnaryOpCode{
    public UnaryOpCodeS(Operand op) {
        super(op);
    }

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        if (op instanceof Register){
            Register _reg = (Register)op;
            List<Code> res = new ArrayList<Code>();
            if (Register.isAbstractRegister(_reg)){
                op = Register.ecx;
                int offset =funcDecl.getOffset(_reg.name);
                res.add(this);
                res.add(new mov(new mem(Register.ebp, BinOp.OpType.SUB, new Const(offset)), Register.ecx));
            }   else {
                res.add(this);
            }
            return res;
        }
        return null;
    }
}
