package ast;

import visitors.Visitor;

import java.util.List;

public class TypeImportOndemandDecl extends ImportDecl {
    public TypeImportOndemandDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public Name getName(){
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
