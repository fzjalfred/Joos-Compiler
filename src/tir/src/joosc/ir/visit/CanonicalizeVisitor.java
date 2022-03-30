package tir.src.joosc.ir.visit;

import tir.src.joosc.ir.ast.CompUnit;
import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.ast.Node;
import tir.src.joosc.ir.ast.Node_c;
import tir.src.joosc.util.InternalCompilerError;
import utils.Pair;

public class CanonicalizeVisitor extends IRVisitor{
    public CanonicalizeVisitor(){
        super(null);
    }

    @Override
    protected Node override(Node parent, Node n) {
        Node n_ = n.visitChildren(this);
        if (n_ == null)
            throw new InternalCompilerError("IRVisitor.visitChildren() returned null!");
        n_.canonicalize();
        return n_;
    }

    public void processComp(CompUnit c){
        for (FuncDecl f : c.functions().values()){
            f.body = f.body.canonicalized_node;
        }
    }
}
