package backend;

import backend.asm.*;
import tir.src.joosc.ir.ast.CompUnit;
import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.List;

public class RegistorAllocator {
    boolean isNaive;
    List<Code> codes;
    private FuncDecl currFuncDecl;
    private CompUnit compUnit;

    public RegistorAllocator(boolean isNaive, List<Code> codes, CompUnit compUnit){
        this.isNaive = isNaive;
        this.codes = codes;
        this.compUnit = compUnit;
        currFuncDecl = null;
    }


    private List<Code> simpleAllocate(){
        List<Code> res = new ArrayList<Code>();
        for (Code code : codes){
            if (code instanceof label){
                label _label = ((label)code);
                if (_label.isFunctionDecl && compUnit.functions().containsKey(_label.name)){
                    currFuncDecl = compUnit.getFunction(_label.name);
                }
                res.add(code);
            }   else if (code instanceof mov && ((mov)code).op1 == Register.ebp && ((mov)code).op2 == Register.esp){
                res.add(code);
                res.add(new sub(Register.esp, new Const(currFuncDecl.chunk.size*4)));
            }   else {
                res.addAll(code.regAllocate(currFuncDecl));
            }
        }
        return res;
    }

    public List<Code> allocate(){
        if (isNaive) return simpleAllocate();
        return null;
    }


}
