package ast;

import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;
import utils.tools;

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
        }   else if (children.get(0) instanceof Name){
            children.set(0, new ArrayType(tools.list(tools.getClassType((Name)children.get(0), null)), ""));
        }   else if (children.get(1) instanceof Dims){
            children.set(0, new ArrayType(tools.list(children.get(0)), ""));
        }
    }

    public Type getType(){
        assert children.get(0) instanceof Type;
        return (Type)children.get(0);
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

    @Override
    public void accept(Visitor v) {
        for (ASTNode node : children) {
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    public UnaryExpr getUnaryExpr(){
        assert children.get(2) instanceof UnaryExpr;
        return (UnaryExpr)children.get(2);
    }
}