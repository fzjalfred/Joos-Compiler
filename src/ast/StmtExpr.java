package ast;

import visitors.Visitor;
import visitors.IRTranslatorVisitor;
import java.util.List;

public class StmtExpr extends ExprStmt {
    public StmtExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
             node.accept(v);
        }
        v.visit(this);
    }

    public Expr getExpr(){
        return (Expr)children.get(0);
    }
}