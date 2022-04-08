package backend.asm;

import tir.src.joosc.ir.ast.FuncDecl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Code {

    @Override
    public String toString() {
        return null;
    }

    public List<Code> regAllocate(FuncDecl funcDecl){
        return new ArrayList<Code>(Arrays.asList(this));
    }
}
