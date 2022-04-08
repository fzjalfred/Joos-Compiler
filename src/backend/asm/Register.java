package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class Register extends Operand{
    public String name;
    static public FuncDecl currFuncDecl = null;
    public Register(String name){
        this.name = name;
        if (currFuncDecl != null){
            currFuncDecl.chunk.vars.add(name);
        }
    }

    public static boolean  isAbstractRegister(Register t){
        return !sysRegs.contains(t);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Register eax = new Register("eax");
    public static Register ebx = new Register("ebx");
    public static Register ecx = new Register("ecx");
    public static Register edx = new Register("edx");
    public static Register esi = new Register("esi");
    public static Register edi = new Register("edi");
    public static Register ebp = new Register("ebp");
    public static Register esp = new Register("esp");
    public static Set<Register> sysRegs = new HashSet<Register>(Arrays.asList(eax,ebx.ecx.edx.esi,edi,ebp,esp));
}
