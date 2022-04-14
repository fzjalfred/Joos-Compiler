package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public abstract class Operand {
    public int value;

    public List<Code> allocateOperand(FuncDecl funcDecl){
        List<Code> res = new ArrayList<Code>();
        if (this instanceof Register){
            Register _reg = (Register)this;
            if (Register.isAbstractRegister(_reg)){
                res.add(new mov(Register.ecx, new mem(Register.ebp, BinOp.OpType.SUB, new Const(funcDecl.getOffset(_reg.name)))));
            }
        }   else if (this instanceof mem){
            mem _mem = (mem)this;
            if (_mem.t1 != null){
                if (Register.isAbstractRegister(_mem.t1)){
                    res.add(new mov(Register.edx, new mem(Register.ebp, BinOp.OpType.SUB, new Const(funcDecl.getOffset(_mem.t1.name)))));
                    _mem.t1 = Register.edx;
                }
                if (_mem.t2 != null){
                    if (Register.isAbstractRegister(_mem.t2)){
                        res.add(new mov(Register.eax, new mem(Register.ebp, BinOp.OpType.SUB, new Const(funcDecl.getOffset(_mem.t2.name)))));
                        _mem.t2 = Register.eax;
                    }
                }
            }   else {
                return null;    // mem case t1 is null
            }
        }   else { // Const
            return null;
        }
        return res;
    }
}
