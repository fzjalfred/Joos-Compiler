package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class jcc extends UnaryOpCodeL{
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
    public ccType cc;

    public jcc(ccType cc, Operand operand){
        super(operand);
        this.cc = cc;
    }
    @Override
    public String toString() {
        return "j"+cc.toString()+" "+op;
    }

}
