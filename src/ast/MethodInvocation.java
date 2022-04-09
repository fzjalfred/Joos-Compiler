package ast;

import lexer.Token;
import visitors.Visitor;

import java.util.List;

public class MethodInvocation extends PrimaryNoArray {
    public Callable whichMethod;
    public Expr receiver;
    public MethodInvocation(List<ASTNode> children, String value){
        super(children, value);
        whichMethod = null;
    }

    public boolean hasName() {
        if (children.size() == 0) {
            return false;
        }
        return (children.get(0) instanceof Name);
    }

    public Name getName() {
        if (!hasName()) return null;
        return (Name) children.get(0);
    }

    public Primary getPrimary(){
        if (hasName()) return null;
        return (Primary) children.get(0);
    }

    public Token getID(){
        if (hasName()) return null;
        return (Token)children.get(1);
     }

    public ArgumentList getArgumentList() {
        if (children.get(2) == null) {
            return null;
        }
        return (ArgumentList) children.get(2);
    }

    public List<Expr> getArgsList(){
        if (getArgumentList() == null) return null;
        return getArgumentList().getArgs();
    }

    public List<Type> getArgumentTypeList(){
        if (getArgumentList() == null) return null;
        return getArgumentList().getArgsType();
    }
    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) {
                node.accept(v);
            }
        }
        v.visit(this);
    }

    @Override
    public String toString() {
        if (hasName()){
            return "MethodInvocation{" +
                    getName().getValue() +
                    '}';
        }   else {
            return "MethodInvocation{" +
                    getPrimary() +
                    '}';
        }
    }
}