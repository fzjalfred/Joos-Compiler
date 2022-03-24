package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.InsnMapsBuilder;

/** An IR function declaration */
public class FuncDecl extends Node_c {
    private String name;
    public Statement body;
    private int numParams;

    public FuncDecl(String name, int numParams, Statement body) {
        this.name = name;
        this.body = body;
        this.numParams = numParams;
    }

    public String name() {
        return name;
    }

    public Statement body() {
        return body;
    }

    public int getNumParams() {
        return numParams;
    }

    @Override
    public String label() {
        return "FUNC " + name;
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        Statement statement = (Statement) v.visit(this, body);

        if (statement != body) return v.nodeFactory().IRFuncDecl(
                name, numParams, statement
        );

        return this;
    }

    @Override
    public String toString() {
        return "FuncDecl{" +
                "name='" + name + '\'' +
                ", body=" + body +
                ", numParams=" + numParams +
                '}';
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(body));
        return result;
    }

    @Override
    public InsnMapsBuilder buildInsnMapsEnter(InsnMapsBuilder v) {
        v.addNameToCurrentIndex(name);
        v.addInsn(this);
        return v;
    }

    @Override
    public Node buildInsnMaps(InsnMapsBuilder v) {
        return this;
    }
}
