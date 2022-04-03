package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.InsnMapsBuilder;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void canonicalize() {
        canonicalized_node = body.canonicalized_node;
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Node> res = new ArrayList<Node>();
        res.add(body);
        return new Pair<List<Node>, Tile>(res, v.unit());
    }
}
