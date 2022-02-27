package ast;

import visitors.Visitor;

import java.util.List;

public class PackageDecl extends ASTNode {
    public PackageDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public Name getName(){
        assert !children.isEmpty();
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
