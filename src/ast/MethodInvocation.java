package ast;

import visitors.Visitor;

import java.util.List;

public class MethodInvocation extends PrimaryNoArray {
    public MethodInvocation(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}