package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import backend.asm.mov;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate representation for a 64-bit integer constant.
 * CONST(n)
 */
public class Const extends Expr_c {
    private int value;

    /**
     *
     * @param value value of this constant
     */
    public Const(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public String label() {
        return "CONST(" + value + ")";
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public int constant() {
        return value;
    }

    @Override
    public String toString() {
        return "Const{" +
                "value=" + value +
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
        codes.codes.add(new mov(res_register, new backend.asm.Const(value)));
        return new Pair<List<Node>, Tile>(nodes,codes);
    }
}
