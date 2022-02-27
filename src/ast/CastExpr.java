package ast;

import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

public class CastExpr extends UnaryExprNotPlusMinus {
    public CastExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    public void changePrefixExprToType(){
        if (children.get(0) instanceof PostFixExpr){
            PostFixExpr postFixExpr = (PostFixExpr)children.get(0);
            assert postFixExpr.getName() != null;
            Name name = postFixExpr.getName();
            children.set(0,new ClassOrInterfaceType(new ArrayList<ASTNode>(){{add(name);}}, ""));
        }
    }

    public Type getType(){
        assert children.get(0) instanceof Type;
        return (Type)children.get(0);
    }
    public boolean hasName() {
        return (children.get(0) instanceof Name);
    }

    public Name getName() {
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    @Override
    public void accept(Visitor v) {
        for (ASTNode node : children) {
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}