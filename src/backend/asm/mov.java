package backend.asm;

import tir.src.joosc.ir.ast.Expr_c;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class mov extends BinaryOpCode{

    public mov(Operand op1, Operand op2){
        super(op1, op2);
    }
    public mov(Operand op1, Operand op2, Expr_c.DataType type){
        super(op1, op2);
        this.size = ToSize(type);
    }

    public enum Size {
        Byte("byte"), Word("word"), Dword("dword");
        private String s;
        private Size(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }

    private Size size = Size.Dword;

    public static Size ToSize(Expr_c.DataType type){
        switch (type){
            case Byte: return Size.Byte;
            case Word: return Size.Word;
            case Dword: return Size.Dword;
        }
        return Size.Dword;
    }

    public String toString(){
		return getClass().getSimpleName()+ " " + size + " " + op1 + "," + op2;
    }
    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        List<Code> res = new ArrayList<Code>();
        if (op1 instanceof Register){
            String op1Name = ((Register)(op1)).name;
            if (op2 instanceof Register && Register.isAbstractRegister((Register)op2)){
                res.add(new mov(Register.edx, mem.genVarAccessMem(funcDecl, ((Register)op2).name)));
                op2 = Register.edx;
                if (Register.isAbstractRegister((Register)op1) ){
                    res.add(new mov(mem.genVarAccessMem(funcDecl, op1Name), Register.edx));
                    return res;
                }
            }   else if (op2 instanceof mem){
                res.addAll(op2.allocateOperand(funcDecl));
            }
            if (Register.isAbstractRegister((Register)op1) ){
                op1 = Register.ecx;
                res.add(this);
                res.add(new mov(mem.genVarAccessMem(funcDecl, op1Name), Register.ecx));
            }   else {
                res.add(this);
            }
        }   else if (op1 instanceof mem){
            res.addAll(op1.allocateOperand(funcDecl));
            if (op2 instanceof Register && Register.isAbstractRegister((Register)op2)){
                res.add(new mov(Register.ecx, mem.genVarAccessMem(funcDecl, ((Register)op2).name)));
                op2 = Register.ecx;
            }
            res.add(this);
        }   else {
            return null;
        }
        return res;
    }
}
