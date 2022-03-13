package ast;

import visitors.Visitor;

import java.util.List;

public class ReturnStmt extends StmtWithoutSubstmt implements AtomicStmt{
    public ReturnStmt(List<ASTNode> children, String value){
        super(children, value);
    }

    public Expr getExpr(){
        assert children.get(0) instanceof Expr;
        return (Expr)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}