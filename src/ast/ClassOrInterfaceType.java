package ast;

import visitors.Visitor;

import java.util.List;

public class ClassOrInterfaceType extends ReferenceType {
    public ClassOrInterfaceType(List<ASTNode> children, String value){
        super(children, value);
    }
    public Name getName(){
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    public boolean equals(ClassOrInterfaceType classOrInterfaceType) {
        Name myName = getName();
        Name hisName = classOrInterfaceType.getName();
        return myName.equals(hisName);
    }

    @Override
    public String getNameString() {
        return getName().getValue();
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    @Override
    public String toString() {
        return getName().getValue();
    }
}
