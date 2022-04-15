package backend.asm;

import tir.src.joosc.ir.ast.BinOp;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class setcc extends UnaryOpCode{
    public enum ccType {
        g("g"), z("z"), e("e"), l("l"), le("le"), nge("nge"), ge("ge"), nl("nl"), nz("nz"), ne("ne");
        private String s;
        private ccType(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }
    public setcc.ccType cc;

    public setcc(setcc.ccType cc, Operand operand){
        super(operand);
        this.cc = cc;
    }
    @Override
    public String toString() {
        return "set"+cc.toString()+" "+op;
    }

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        List<Code> res = new ArrayList<Code>();
        if (op instanceof Register && Register.isAbstractRegister((Register)op)){
            Register _reg = (Register)op;
            int offset =funcDecl.getOffset(_reg.name);
            res.add(new setcc(cc, Register.ecx.l));
            res.add(new mov(new mem(Register.ebp, BinOp.OpType.SUB, new Const(offset)), Register.ecx));
            return res;
        }
        res.add(this);
        return res;
    }
}
