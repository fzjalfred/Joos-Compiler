package ast;

import visitors.Visitor;

import java.util.List;

public class MethodInvocation extends PrimaryNoArray {

    public MethodInvocation(List<ASTNode> children, String value){
        super(children, value);
    }

    public boolean hasName() {
        if (children.size() == 0) {
            return false;
        }
        return (children.get(0) instanceof Name);
    }

    public Name getName() {
        assert children.get(0) instanceof Name;
        return (Name) children.get(0);
    }

    public ArgumentList getArgumentList() {
        if (children.get(2) == null) {
            return null;
        }
        return (ArgumentList) children.get(2);
    }

    public List<Type> getArgumentTypeList(){
        if (getArgumentList() == null) return null;
        return getArgumentList().getArgsType();
    }
    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}