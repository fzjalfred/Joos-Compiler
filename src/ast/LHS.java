package ast;

import visitors.Visitor;

import java.util.List;

public class LHS extends ASTNode {
    public LHS(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);

    public boolean hasName() {
        return (children.get(0) instanceof Name);
    }

    public Name getName() {
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }
}