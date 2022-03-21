package tir.src.joosc.ir.ast;

public interface Expr extends Node {
    boolean isConstant();

    int constant();
}
