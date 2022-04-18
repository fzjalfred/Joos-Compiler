package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.ast.Temp;
import tir.src.joosc.ir.interpret.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class Register extends Operand{
    public String name;
    static public FuncDecl currFuncDecl = null;
    public Register l;
    public Register h;
    public Register x;
    public Register(String name){
        this.name = name;

        if (currFuncDecl != null){
            currFuncDecl.chunk.vars.add(name);
        }
        l = null;
        h = null;
        x = null;
    }

    public Register(String name, String lstr, String hstr, String xstr){

        this.name = name;

        if (currFuncDecl != null){
            currFuncDecl.chunk.vars.add(name);
        }
        l = new Register(lstr);
        h = new Register(hstr);
        x = new Register(xstr);
    }

    public static boolean  isAbstractRegister(Register t){
        return !sysRegs.contains(t);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Register eax = new Register("eax", "al", "ah", "ax");
    public static Register ebx = new Register("ebx", "bl", "bh", "bx");
    public static Register ecx = new Register("ecx", "cl", "ch", "cx");
    public static Register edx = new Register("edx", "dl", "dh", "dx");
    public static Register esi = new Register("esi");
    public static Register edi = new Register("edi");
    public static Register ebp = new Register("ebp");
    public static Register esp = new Register("esp");
    public static Set<Register> sysRegs = new HashSet<Register>(Arrays.asList(eax,ebx,ecx,edx,esi,edi,ebp,esp));

    public static Register tempToReg(Temp t){
        if (t.name().equals(Configuration.ABSTRACT_RET)) return eax;
        return new Register(t.name());
    }
}
