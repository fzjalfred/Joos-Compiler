package ast;

import visitors.Visitor;

import java.util.List;
import java.util.ArrayList;

public class ArgumentList extends ASTNode {
    private List<Expr> args;
    private List<Type> argsTypes;
    public ArgumentList(List<ASTNode> children, String value){
        super(children, value);
        args = null;
        argsTypes = null;
    }

    public List<Expr> getArgs(){
        if(args != null) return args;
        args = new ArrayList<Expr>();
        for (ASTNode node : children){
            assert node instanceof Expr;
            args.add((Expr)node);
        }
        return args;
    }

    public List<Type> getArgsType(){
        argsTypes = new ArrayList<Type>();
        for (ASTNode node : children){
            assert node instanceof Expr;
            Expr expr = (Expr)node;
            //System.out.print(expr.type);
            argsTypes.add(expr.type);
        }
        return argsTypes;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}