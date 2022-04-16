package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import exception.BackendError;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CanonicalizeVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.*;

/**
 * An intermediate representation for a compilation unit
 */
public class CompUnit extends Node_c {
    private String name;
    private Map<String, FuncDecl> functions;
    public Map<String, String> stringLiteralToLabel;
    public Set<String> externStrs;

    public CompUnit(String name) {
        this.name = name;
        functions = new LinkedHashMap<>();
        externStrs = new HashSet<String>();
        stringLiteralToLabel = new HashMap<>();
    }

    public CompUnit(String name, Map<String, FuncDecl> functions) {
        this.name = name;
        this.functions = functions;
        externStrs = new HashSet<String>();
        stringLiteralToLabel = new HashMap<>();
    }

    public void appendFunc(FuncDecl func) {
        functions.put(func.name(), func);
    }

    public String name() {
        return name;
    }

    public Map<String, FuncDecl> functions() {
        return functions;
    }

    public FuncDecl getFunction(String name) {
        return functions.get(name);
    }

    @Override
    public String label() {
        return "COMPUNIT";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        boolean modified = false;

        Map<String, FuncDecl> results = new LinkedHashMap<>();
        for (FuncDecl func : functions.values()) {
            FuncDecl newFunc = (FuncDecl) v.visit(this, func);
            if (newFunc != func) modified = true;
            results.put(newFunc.name(), newFunc);
        }

        if (modified) return v.nodeFactory().IRCompUnit(name, results);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        for (FuncDecl func : functions.values())
            result = v.bind(result, v.visit(func));
        return result;
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq();
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        BackendError.currFile = name;
        List<Node> res = new ArrayList<Node>();
        for (FuncDecl f : functions.values()){
            res.add(f);
        }
        return new Pair<List<Node>, Tile>(res, v.unit());
    }
}
