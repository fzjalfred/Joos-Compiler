package tir.src.joosc.ir.ast;

import backend.asm.Register;
import backend.asm.Tile;
import backend.asm.mov;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate representation for a temporary register
 * TEMP(name)
 */
public class Temp extends Expr_c {
    private String name;

    /**
     * @param name name of this temporary register
     */
    public Temp(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String label() {
        return "TEMP(" + name + ")";
    }

    public Temp(String name, DataType type) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Temp{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq(new Exp(this));
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Node> nodes = new ArrayList<Node>();
        Tile codes = v.unit();
        codes.codes.add(new mov(res_register, new Register(name)));
        return new Pair<List<Node>, Tile>(nodes,codes);
    }
}
