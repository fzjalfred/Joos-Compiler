package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import tir.src.joosc.ir.visit.*;
import utils.Pair;
import java.util.List;

/**
 * A node in an intermediate-representation abstract syntax tree.
 */
public interface Node {
    /**
     * Visit the children of this IR node.
     * @param v the visitor
     * @return the result of visiting children of this node
     */
    Node visitChildren(IRVisitor v);

    InsnMapsBuilder buildInsnMapsEnter(InsnMapsBuilder v);

    Node buildInsnMaps(InsnMapsBuilder v);

    <T> T aggregateChildren(AggregateVisitor<T> v);

    CheckCanonicalIRVisitor checkCanonicalEnter(CheckCanonicalIRVisitor v);

    boolean isCanonical(CheckCanonicalIRVisitor v);

    String label();

    void canonicalize();

    Pair<List<Node>, Tile> tiling(TilingVisitor v); // return nodes to be visited and tile result
}
