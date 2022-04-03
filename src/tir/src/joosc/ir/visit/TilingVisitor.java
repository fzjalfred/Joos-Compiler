package tir.src.joosc.ir.visit;

import backend.asm.Tile;
import tir.src.joosc.ir.ast.Node;
import utils.Pair;

import java.util.List;

public class TilingVisitor extends AggregateVisitor<Tile>{

    @Override
    public Tile unit() {
        return new Tile();
    }

    @Override
    public Tile bind(Tile r1, Tile r2) {
        r1.codes.addAll(r2.codes);
        return r1;
    }

    @Override
    protected Tile override(Node parent, Node n) {
        Pair<List<Node>, Tile> res_pair = n.tiling(this);
        Tile res = unit();
        for (Node node : res_pair.first){
            res = bind(res, visit(node));
        }
        res = bind(res, res_pair.second);
        return res;
    }
}
