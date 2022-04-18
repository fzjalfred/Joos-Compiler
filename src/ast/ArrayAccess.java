package ast;

import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

public class ArrayAccess extends PrimaryNoArray {

    public List<FieldDecl> subfields = null;
    public ArrayAccess(List<ASTNode> children, String value){
        super(children, value);
        subfields = new ArrayList<FieldDecl>();
    }

    public boolean hasName() {
        if (children.size() == 0) {
            return false;
        }
        return (children.get(0) instanceof Name);
    }

    public Name getName() {
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);

    }

    public PrimaryNoArray getExpr(){
        if (hasName()) return null;
        return (PrimaryNoArray)children.get(0);
    }

    public DimExpr getDimExpr(){
        return (DimExpr)children.get(1);
    }

    @Override
    public void accept(Visitor v) {
        for (ASTNode node : children) {
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}