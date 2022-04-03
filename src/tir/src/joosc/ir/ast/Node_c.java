package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import tir.src.joosc.ir.visit.*;
import utils.Pair;

import java.util.List;

/**
 * A node in an intermediate-representation abstract syntax tree.
 */
public abstract class Node_c implements Node {

    @Override
    public Node visitChildren(IRVisitor v) {
        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        return v.unit();
    }

    @Override
    public InsnMapsBuilder buildInsnMapsEnter(InsnMapsBuilder v) {
        return v;
    }

    @Override
    public Node buildInsnMaps(InsnMapsBuilder v) {
        v.addInsn(this);
        return this;
    }

    @Override
    public CheckCanonicalIRVisitor checkCanonicalEnter(
            CheckCanonicalIRVisitor v) {
        return v;
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return true;
    }

    @Override
    public abstract String label();

    @Override
    public void canonicalize() {
    }

    public Seq canonicalized_node = null;

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        return null;
    }
}
