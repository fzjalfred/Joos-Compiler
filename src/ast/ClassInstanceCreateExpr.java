package ast;

import visitors.Visitor;

import java.util.List;

public class ClassInstanceCreateExpr extends PrimaryNoArray {
    public Callable callable;
    public Expr receiver;
    public ClassInstanceCreateExpr(List<ASTNode> children, String value){
        super(children, value);
        callable = null;
    }

    public ClassOrInterfaceType getType(){
        assert (children.get(0) instanceof ClassOrInterfaceType);
        return (ClassOrInterfaceType)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    public ArgumentList getArgumentList(){
        assert (children.get(1) instanceof ArgumentList);
        return (ArgumentList)children.get(1);
    }

    public List<Type> getArgumentTypeList(){
        if (getArgumentList() == null) return null;
        return getArgumentList().getArgsType();
    }
}