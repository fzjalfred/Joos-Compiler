package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import tir.src.joosc.ir.visit.InsnMapsBuilder;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate representation for naming a memory address
 */
public class Label extends Statement {
    private String name;

    /**
     *
     * @param name name of this memory address
     */
    public Label(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String label() {
        return "LABEL(" + name + ")";
    }

    @Override
    public String toString() {
        return "Label{" +
                "name=" + name +
                '}';
    }

    @Override
    public InsnMapsBuilder buildInsnMapsEnter(InsnMapsBuilder v) {
        v.addNameToCurrentIndex(name);
        return v;
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq(this);
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        return new Pair<List<Node>, Tile>(new ArrayList<Node>(), v.unit()); //TODO
    }
}
