package tir.src.joosc.ir.ast;

import backend.asm.*;
import backend.asm.Const;
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

    public Label removeFuncLabel(){
        return ((Seq)body).removeFirst();
    }

    public Tile getPrologue(TilingVisitor v) {
        List<Code> tileCodes = new ArrayList<Code>();

        Label l = removeFuncLabel();
        tileCodes.add(new label(l.name()));
        tileCodes.add(new push(Register.ebp));
        tileCodes.add(new mov(Register.ebp, Register.esp));
        //tileCodes.add(new sub(Register.esp, new Const(4*32))); // not sure
        return new Tile(tileCodes);
    }

    public Tile getEpilogue(TilingVisitor v) {
        List<Code> tileCodes = new ArrayList<Code>();
        tileCodes.add(new mov(Register.esp, Register.ebp));
        tileCodes.add(new pop(Register.ebp));
        tileCodes.add(new ret());
        return new Tile(tileCodes);
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
