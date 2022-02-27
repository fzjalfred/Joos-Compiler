package ast;

import visitors.TypeCheckVisitor;
import visitors.Visitor;

import java.util.List;

public class ClassBody extends ASTNode {
    public ClassBody(List<ASTNode> children, String value){
        super(children, value);
    }

    private void acceptMain(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    @Override
    public void accept(Visitor v){
        if (v instanceof TypeCheckVisitor){
            TypeCheckVisitor visitor = (TypeCheckVisitor)v;
            visitor.context.entry();
            acceptMain(v);
            visitor.context.pop();
        }   else{
            acceptMain(v);
        }
    }
}
