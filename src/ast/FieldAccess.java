package ast;

import lexer.Token;
import visitors.Visitor;

import java.util.List;

public class FieldAccess extends PrimaryNoArray {
    public FieldAccess(List<ASTNode> children, String value){
        super(children, value);
    }

    public Token getID(){
        assert children.get(1) instanceof Token;
        return (Token)children.get(1);
    }

    public Primary getPrimary(){
        return (Primary)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}