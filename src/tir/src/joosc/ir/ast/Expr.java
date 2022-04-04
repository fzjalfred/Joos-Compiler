package tir.src.joosc.ir.ast;

import backend.asm.Register;

public interface Expr extends Node {
    boolean isConstant();
    void setResReg(Register t);
    int constant();
}
