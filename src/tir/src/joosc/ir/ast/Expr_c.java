package tir.src.joosc.ir.ast;

import backend.asm.Register;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;

/**
 * An intermediate representation for expressions
 */
public abstract class Expr_c extends Node_c implements Expr {
    public DataType type = DataType.Dword;
    public enum DataType {
        Byte, Word, Dword
    }
    @Override
    public CheckCanonicalIRVisitor checkCanonicalEnter(
            CheckCanonicalIRVisitor v) {
        return v.enterExpr();
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return v.inExpr() || !v.inExp();
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public int constant() {
        throw new UnsupportedOperationException();
    }

    public Register res_register = null;

    @Override
    public void setResReg(Register t) {
        res_register = t;
    }
}
