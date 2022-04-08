package tir.src.joosc.ir.ast;

import backend.asm.Tile;

import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;
import java.util.List;

import backend.asm.*;
import java.util.ArrayList;
/**
 * An intermediate representation for named memory address
 * NAME(n)
 */
public class Name extends Expr_c {
    private String name;

    /**
     *
     * @param name name of this memory address
     */
    public Name(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String label() {
        return "NAME(" + name + ")";
    }

    @Override
    public String toString() {
        return "Name{" +
                "name=" + name +
                '}';
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq(new Exp(this));
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        // List<Code> tileCodes = new ArrayList<Code>();
        // List<Node> nodes = new ArrayList<Node>();
        
        // tileCodes.add(new jmp(new LabelOperand(name)));
        
        // return new Pair<List<Node>,Tile>(nodes, new Tile(tileCodes));
        return null;
    }
}
