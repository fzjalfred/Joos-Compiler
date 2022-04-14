package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class test extends BinaryOpCode{
    public test(Operand op1, Operand op2){
        super(op1, op2);
    }

    @Override
    public List<Code> regAllocate(FuncDecl funcDecl) {
        List<Code> res = new ArrayList<Code>();
        if (op1 instanceof Register){
            String op1Name = ((Register)(op1)).name;
            if (op2 instanceof Register && Register.isAbstractRegister((Register)op2)){
                res.add(new test(Register.edx, mem.genVarAccessMem(funcDecl, ((Register)op2).name)));
                op2 = Register.edx;
                if (Register.isAbstractRegister((Register)op1) ){
                    res.add(new test(mem.genVarAccessMem(funcDecl, op1Name), Register.edx));
                    return res;
                }
            }   else if (op2 instanceof mem){
                res.addAll(op2.allocateOperand(funcDecl));
            }
            if (Register.isAbstractRegister((Register)op1) ){
                op1 = Register.ecx;
                res.add(this);
                res.add(new test(mem.genVarAccessMem(funcDecl, op1Name), Register.ecx));
            }   else {
                res.add(this);
            }
        }   else if (op1 instanceof mem){
            res.addAll(op1.allocateOperand(funcDecl));
            if (op2 instanceof Register && Register.isAbstractRegister((Register)op2)){
                res.add(new test(Register.ecx, mem.genVarAccessMem(funcDecl, ((Register)op2).name)));
                op2 = Register.ecx;
            }
            res.add(this);
        }   else {
            return null;
        }
        return res;
    }
}
