package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class jcc extends UnaryOpCode{
    public enum ccType {
        z("z"), e("e"), l("l"), nge("nge"), ge("ge"), nl("nl"), nz("nz");
        private String s;
        private ccType(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }
    public ccType cc;

    public jcc(ccType cc, Operand operand){
        super(operand);
        this.cc = cc;
    }
    @Override
    public String toString() {
        return "j"+cc.toString()+" "+op;
    }

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        List<Code> res = new ArrayList<Code>();
        if (op instanceof Register && Register.isAbstractRegister((Register)op)){
            res.add(new mov(Register.ecx, mem.genVarAccessMem(funcDecl, ((Register)op).name)));
            res.add(new jcc(cc, Register.ecx));
            return res;
        }
        res.add(this);
        return res;
    }
}
