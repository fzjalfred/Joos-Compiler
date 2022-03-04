package ast;

import visitors.Visitor;

import java.util.List;

public class VarDeclarator extends ASTNode{
    public VarDeclarator(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        VarDeclaratorID vid = getVarDeclaratorID();
        return vid.getName();
    }
    public VarDeclaratorID getVarDeclaratorID(){
        assert children.get(0) instanceof VarDeclaratorID;
        return (VarDeclaratorID)children.get(0);
    }

    public Expr getExpr() {
        assert children.get(1) instanceof Expr;
        return (Expr) children.get(1);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
