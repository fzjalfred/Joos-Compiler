package ast;

import visitors.UnreachableStmtVisitor;
import visitors.Visitor;

import java.util.List;

public class WhileStmt extends Stmt {
    public WhileStmt(List<ASTNode> children, String value){
        super(children, value);
    }
    public Expr getExpr(){
        assert children.get(0) instanceof Expr;
        return (Expr)children.get(0);
    }
    public Stmt getStmt(){
        assert children.get(1) instanceof BlockStmt;
        return (Stmt) children.get(1);
    }

    @Override
    public void accept(Visitor v){
        if (v instanceof UnreachableStmtVisitor){
            v.visit(this);
        }   else {
            for (ASTNode node: children){
                if (node != null) node.accept(v);
            }
            v.visit(this);
        }

    }
}